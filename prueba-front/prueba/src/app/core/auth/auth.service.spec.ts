import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from './auth.service';
import { LoanApiService } from '../../features/loans/data-access/loan-api.service';

describe('AuthService', () => {
  let auth: AuthService;
  let loans: LoanApiService;
  let http: HttpClient;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    auth = TestBed.inject(AuthService);
    loans = TestBed.inject(LoanApiService);
    http = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('keeps the signed-in role and adds its bearer token to API requests', () => {
    auth.login({ username: 'client', password: 'password' }).subscribe();
    httpTesting.expectOne('http://localhost:8080/api/auth/login').flush({
      accessToken: 'signed-token',
      tokenType: 'Bearer',
      expiresIn: 1800,
      username: 'client',
      role: 'CUSTOMER',
    });

    expect(auth.currentUser()?.role).toBe('CUSTOMER');
    const storedSession = JSON.parse(
      localStorage.getItem('loan-api.auth-session') ?? '{}',
    ) as { user: { username: string }; expiresAt: number };
    expect(storedSession.user.username).toBe('client');
    expect(storedSession.expiresAt - Date.now()).toBeLessThanOrEqual(10 * 60 * 1000);
    expect(storedSession.expiresAt - Date.now()).toBeGreaterThan(9 * 60 * 1000);
    loans.list().subscribe();
    const request = httpTesting.expectOne('http://localhost:8080/api/loans');
    expect(request.request.headers.get('Authorization')).toBe('Bearer signed-token');
    request.flush([]);

    http.get('https://example.test/ping').subscribe();
    const externalRequest = httpTesting.expectOne('https://example.test/ping');
    expect(externalRequest.request.headers.has('Authorization')).toBe(false);
    externalRequest.flush({});
  });

  it('clears credentials when the user signs out', () => {
    auth.login({ username: 'admin', password: 'password' }).subscribe();
    httpTesting.expectOne('http://localhost:8080/api/auth/login').flush({
      accessToken: 'admin-token',
      tokenType: 'Bearer',
      expiresIn: 1800,
      username: 'admin',
      role: 'ADMIN',
    });

    auth.logout();

    expect(auth.currentUser()).toBeNull();
    expect(auth.token()).toBeNull();
    expect(localStorage.getItem('loan-api.auth-session')).toBeNull();
  });

  it('restores a session after the service is recreated', () => {
    auth.login({ username: 'client', password: 'password' }).subscribe();
    httpTesting.expectOne('http://localhost:8080/api/auth/login').flush({
      accessToken: 'signed-token',
      tokenType: 'Bearer',
      expiresIn: 1800,
      username: 'client',
      role: 'CUSTOMER',
    });

    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    auth = TestBed.inject(AuthService);
    httpTesting = TestBed.inject(HttpTestingController);

    expect(auth.currentUser()?.username).toBe('client');
    expect(auth.token()).toBe('signed-token');
  });

  it('discards an expired persisted session', () => {
    localStorage.setItem(
      'loan-api.auth-session',
      JSON.stringify({
        user: {
          accessToken: 'expired-token',
          tokenType: 'Bearer',
          expiresIn: 1800,
          username: 'client',
          role: 'CUSTOMER',
        },
        expiresAt: Date.now() - 1,
      }),
    );

    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    auth = TestBed.inject(AuthService);
    httpTesting = TestBed.inject(HttpTestingController);

    expect(auth.currentUser()).toBeNull();
    expect(auth.token()).toBeNull();
    expect(localStorage.getItem('loan-api.auth-session')).toBeNull();
  });
});
