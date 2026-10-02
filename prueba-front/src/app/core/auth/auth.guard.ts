import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const customerGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  if (!auth.currentUser()) {
    return inject(Router).createUrlTree(['/login']);
  }
  return auth.currentUser()?.role === 'CUSTOMER'
    ? true
    : inject(Router).createUrlTree(['/admin/loans']);
};

export const authenticatedGuard: CanActivateFn = () => {
  return inject(AuthService).currentUser()
    ? true
    : inject(Router).createUrlTree(['/login']);
};

export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  if (!auth.currentUser()) {
    return inject(Router).createUrlTree(['/login']);
  }
  return auth.currentUser()?.role === 'ADMIN'
    ? true
    : inject(Router).createUrlTree(['/loans']);
};

export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  if (!auth.currentUser()) {
    return true;
  }
  return inject(Router).createUrlTree([
    auth.currentUser()?.role === 'ADMIN' ? '/admin/loans' : '/loans',
  ]);
};
