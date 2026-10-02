import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { LoanApiService } from '../data-access/loan-api.service';
import { Loan, LoanStatus } from '../models/loan';
import {
  formatLoanAmount,
  formatLoanDate,
  loanErrorMessage,
  loanStatusLabel,
} from '../models/loan-view.utils';

@Component({
  selector: 'app-admin-loans-page',
  templateUrl: './admin-loans-page.html',
})
export class AdminLoansPage {
  private readonly changeDetector = inject(ChangeDetectorRef);
  private readonly loansApi = inject(LoanApiService);

  protected loans: Loan[] = [];
  protected loading = false;
  protected error = '';
  protected notice = '';

  constructor() {
    this.loadLoans();
  }

  protected decide(loan: Loan, status: Extract<LoanStatus, 'APPROVED' | 'REJECTED'>): void {
    this.error = '';
    this.notice = '';
    this.loansApi.decide(loan.id, status).subscribe({
      next: () => {
        this.notice = `La solicitud #${loan.id} fue ${status === 'APPROVED' ? 'aprobada' : 'rechazada'}.`;
        this.changeDetector.markForCheck();
        this.loadLoans();
      },
      error: (error: HttpErrorResponse) => {
        this.error = loanErrorMessage(error);
        this.changeDetector.markForCheck();
      },
    });
  }

  protected statusLabel = loanStatusLabel;
  protected formatAmount = formatLoanAmount;
  protected formatDate = formatLoanDate;

  private loadLoans(): void {
    this.loading = true;
    this.loansApi.list().subscribe({
      next: (loans) => {
        this.loans = loans;
        this.loading = false;
        this.changeDetector.markForCheck();
      },
      error: (error: unknown) => {
        this.loading = false;
        this.error = loanErrorMessage(error);
        this.changeDetector.markForCheck();
      },
    });
  }
}
