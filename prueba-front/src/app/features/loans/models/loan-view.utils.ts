import { HttpErrorResponse } from '@angular/common/http';
import { TimeoutError } from 'rxjs';
import { LoanStatus } from './loan';

export function loanErrorMessage(error: unknown): string {
  if (error instanceof TimeoutError) {
    return 'La API tardó demasiado en responder. Verifica que el backend y la base de datos estén disponibles e inténtalo de nuevo.';
  }
  if (!(error instanceof HttpErrorResponse)) {
    return 'Ocurrió un error inesperado al procesar la solicitud.';
  }
  if (error.status === 0) {
    return 'No se pudo conectar con la API. Verifica que el backend esté activo en localhost:8080.';
  }
  if (error.status === 400) {
    return 'Los datos no son válidos. Verifica el monto y el plazo ingresados.';
  }
  if (error.status === 401) {
    return 'Tu sesión expiró. Inicia sesión nuevamente.';
  }
  if (error.status === 403) {
    return 'Tu rol no tiene permiso para realizar esta acción.';
  }
  return error.error?.detail ?? 'Ocurrió un error al procesar la solicitud.';
}

export function loanStatusLabel(status: LoanStatus): string {
  return {
    PENDING: 'En revisión',
    APPROVED: 'Aprobado',
    REJECTED: 'Rechazado',
  }[status];
}

export function formatLoanAmount(amount: number): string {
  return new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    maximumFractionDigits: 0,
  }).format(amount);
}

export function formatLoanDate(date: string): string {
  return new Intl.DateTimeFormat('es-CO', { dateStyle: 'medium' }).format(new Date(date));
}
