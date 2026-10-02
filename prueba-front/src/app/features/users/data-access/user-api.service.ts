import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, timeout } from 'rxjs';
import { API_BASE_URL } from '../../../core/config/api';
import { AdminUserUpdate, ProfileUpdate, User } from '../models/user';

@Injectable({ providedIn: 'root' })
export class UserApiService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_BASE_URL}/users`;

  current(): Observable<User> {
    return this.http.get<User>(`${this.url}/me`).pipe(timeout({ first: 15000 }));
  }

  updateCurrent(request: ProfileUpdate): Observable<User> {
    return this.http.put<User>(`${this.url}/me`, request);
  }

  list(): Observable<User[]> {
    return this.http.get<User[]>(this.url).pipe(timeout({ first: 15000 }));
  }

  update(id: number, request: AdminUserUpdate): Observable<User> {
    return this.http.put<User>(`${this.url}/${id}`, request);
  }
}
