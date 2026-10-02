import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { Subject, TimeoutError } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { LoanApiService } from '../data-access/loan-api.service';
import { Loan } from '../models/loan';
import { CustomerLoansPage } from './customer-loans-page';

describe('CustomerLoansPage', () => {
  let loanList: Subject<Loan[]>;

  beforeEach(async () => {
    loanList = new Subject<Loan[]>();
    await TestBed.configureTestingModule({
      imports: [CustomerLoansPage],
      providers: [
        provideHttpClient(),
        {
          provide: AuthService,
          useValue: {
            currentUser: signal({
              accessToken: 'token',
              tokenType: 'Bearer',
              expiresIn: 1800,
              username: 'customer',
              role: 'CUSTOMER',
            }),
          },
        },
        {
          provide: LoanApiService,
          useValue: { list: () => loanList.asObservable() },
        },
      ],
    }).compileComponents();
  });

  it('shows the empty state when there are no loan applications', () => {
    const fixture = TestBed.createComponent(CustomerLoansPage);
    fixture.detectChanges();
    loanList.next([]);
    loanList.complete();
    fixture.detectChanges();

    const content = (fixture.nativeElement as HTMLElement).textContent;
    expect(content).toContain('Aún no hay solicitudes');
    expect(content).not.toContain('Cargando solicitudes...');
  });

  it('shows an error instead of loading forever when the request times out', () => {
    const fixture = TestBed.createComponent(CustomerLoansPage);
    fixture.detectChanges();
    loanList.error(new TimeoutError());
    fixture.detectChanges();

    const content = (fixture.nativeElement as HTMLElement).textContent;
    expect(content).toContain('La API tardó demasiado en responder');
    expect(content).not.toContain('Cargando solicitudes...');
  });
});
