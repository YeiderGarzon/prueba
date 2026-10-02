export type LoanStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface Loan {
  id: number;
  applicantId: number;
  applicantUsername: string;
  applicantFullName: string;
  amount: number;
  termMonths: number;
  status: LoanStatus;
  createdAt: string;
}

/** The applicant is derived from the authenticated user by the API. */
export interface LoanRequest {
  amount: number;
  termMonths: number;
}
