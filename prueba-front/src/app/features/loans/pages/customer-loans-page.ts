import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/auth/auth.service';
import { LoanApiService } from '../data-access/loan-api.service';
import { Loan } from '../models/loan';
import {
  formatLoanAmount,
  formatLoanDate,
  loanErrorMessage,
  loanStatusLabel,
} from '../models/loan-view.utils';

@Component({
  imports: [FormsModule],
  selector: 'app-customer-loans-page',
  templateUrl: './customer-loans-page.html',
})
export class CustomerLoansPage {
  protected readonly auth = inject(AuthService);
  private readonly changeDetector = inject(ChangeDetectorRef);
  private readonly loansApi = inject(LoanApiService);

  protected amount: number | null = null;
  protected termMonths: number | null = null;
  protected loans: Loan[] = [];
  protected loading = false;
  protected submitting = false;
  protected error = '';
  protected notice = '';

  constructor() {
    this.loadLoans();
  }

  protected submitApplication(): void {
    this.error = '';
    this.notice = '';
    if (this.amount === null || this.termMonths === null) {
      this.error = 'Completa el monto y el plazo para enviar la solicitud.';
      return;
    }

    this.submitting = true;
    this.loansApi.create({ amount: this.amount, termMonths: this.termMonths }).subscribe({
      next: (loan) => {
        this.submitting = false;
        this.notice = `Solicitud #${loan.id} enviada correctamente.`;
        this.amount = null;
        this.termMonths = null;
        this.changeDetector.markForCheck();
        this.loadLoans();
      },
      error: (error: HttpErrorResponse) => {
        this.submitting = false;
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
