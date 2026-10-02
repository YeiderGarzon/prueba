import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { API_BASE_URL } from '../config/api';
import { AuthResponse, Credentials, RegistrationDetails } from './auth';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private static readonly SESSION_STORAGE_KEY = 'loan-api.auth-session';
  private static readonly SESSION_DURATION_MS = 10 * 60 * 1000;

  private readonly http = inject(HttpClient);
  private readonly url = `${API_BASE_URL}/auth`;
  private readonly accessToken = signal<string | null>(null);
  readonly currentUser = signal<AuthResponse | null>(null);
  private expiryTimer: ReturnType<typeof setTimeout> | null = null;

  constructor() {
    this.restoreSession();
  }

  login(credentials: Credentials): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.url}/login`, credentials).pipe(
      tap((response) => {
        const expiresAt = Date.now() + AuthService.SESSION_DURATION_MS;
        localStorage.setItem(
          AuthService.SESSION_STORAGE_KEY,
          JSON.stringify({ user: response, expiresAt }),
        );
        this.accessToken.set(response.accessToken);
        this.currentUser.set(response);
        this.scheduleLogout(expiresAt);
      }),
    );
  }

  register(details: RegistrationDetails): Observable<void> {
    return this.http.post<void>(`${this.url}/register`, details);
  }

  token(): string | null {
    this.clearIfExpired();
    return this.accessToken();
  }

  logout(): void {
    if (this.expiryTimer !== null) {
      clearTimeout(this.expiryTimer);
      this.expiryTimer = null;
    }
    localStorage.removeItem(AuthService.SESSION_STORAGE_KEY);
    this.accessToken.set(null);
    this.currentUser.set(null);
  }

  private restoreSession(): void {
    const stored = localStorage.getItem(AuthService.SESSION_STORAGE_KEY);
    if (!stored) {
      return;
    }

    let session: unknown;
    try {
      session = JSON.parse(stored);
    } catch {
      this.logout();
      return;
    }

    if (!this.isStoredSession(session) || session.expiresAt <= Date.now()) {
      this.logout();
      return;
    }

    this.accessToken.set(session.user.accessToken);
    this.currentUser.set(session.user);
    this.scheduleLogout(session.expiresAt);
  }

  private isStoredSession(
    value: unknown,
  ): value is { user: AuthResponse; expiresAt: number } {
    if (typeof value !== 'object' || value === null) {
      return false;
    }
    const session = value as Record<string, unknown>;
    const user = session['user'];
    if (typeof user !== 'object' || user === null) {
      return false;
    }
    const authResponse = user as Record<string, unknown>;
    return (
      typeof session['expiresAt'] === 'number' &&
      Number.isFinite(session['expiresAt']) &&
      typeof authResponse['accessToken'] === 'string' &&
      authResponse['accessToken'].length > 0 &&
      authResponse['tokenType'] === 'Bearer' &&
      typeof authResponse['expiresIn'] === 'number' &&
      typeof authResponse['username'] === 'string' &&
      (authResponse['role'] === 'ADMIN' || authResponse['role'] === 'CUSTOMER')
    );
  }

  private scheduleLogout(expiresAt: number): void {
    if (this.expiryTimer !== null) {
      clearTimeout(this.expiryTimer);
    }
    this.expiryTimer = setTimeout(
      () => this.logout(),
      Math.max(0, expiresAt - Date.now()),
    );
  }

  private clearIfExpired(): void {
    const stored = localStorage.getItem(AuthService.SESSION_STORAGE_KEY);
    if (!stored) {
      if (this.currentUser()) {
        this.logout();
      }
      return;
    }

    try {
      const session: unknown = JSON.parse(stored);
      if (
        !this.isStoredSession(session) ||
        session.expiresAt <= Date.now()
      ) {
        this.logout();
      }
    } catch {
      this.logout();
    }
  }
}
