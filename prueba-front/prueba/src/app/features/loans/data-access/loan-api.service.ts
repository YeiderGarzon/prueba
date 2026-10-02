import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, timeout } from 'rxjs';
import { API_BASE_URL } from '../../../core/config/api';
import { Loan, LoanRequest, LoanStatus } from '../models/loan';

@Injectable({ providedIn: 'root' })
export class LoanApiService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_BASE_URL}/loans`;

  create(request: LoanRequest): Observable<Loan> {
    return this.http.post<Loan>(this.url, request);
  }

  list(): Observable<Loan[]> {
    return this.http.get<Loan[]>(this.url).pipe(timeout({ first: 15000 }));
  }

  decide(id: number, status: Extract<LoanStatus, 'APPROVED' | 'REJECTED'>): Observable<Loan> {
    return this.http.patch<Loan>(`${this.url}/${id}/decision`, { status });
  }
}
