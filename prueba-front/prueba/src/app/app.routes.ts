import { Routes } from '@angular/router';
import {
  authenticatedGuard,
  adminGuard,
  customerGuard,
  guestGuard,
} from './core/auth/auth.guard';
import { AuthPage } from './features/auth/pages/auth-page';
import { AdminLoansPage } from './features/loans/pages/admin-loans-page';
import { CustomerLoansPage } from './features/loans/pages/customer-loans-page';
import { ProfilePage } from './features/users/pages/profile-page';
import { AdminUsersPage } from './features/users/pages/admin-users-page';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: 'login', component: AuthPage, canActivate: [guestGuard] },
  { path: 'loans', component: CustomerLoansPage, canActivate: [customerGuard] },
  { path: 'admin/loans', component: AdminLoansPage, canActivate: [adminGuard] },
  { path: 'profile', component: ProfilePage, canActivate: [authenticatedGuard] },
  { path: 'admin/users', component: AdminUsersPage, canActivate: [adminGuard] },
  { path: '**', redirectTo: 'login' },
];
