import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { AuthResponse } from './auth';
import { AuthService } from './auth.service';
import { adminGuard, authenticatedGuard, customerGuard } from './auth.guard';

describe('authorization guards', () => {
  const currentUser = signal<AuthResponse | null>(null);

  beforeEach(() => {
    currentUser.set(null);
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: { currentUser },
        },
      ],
    });
  });

  it('redirects unauthenticated users to login', () => {
    const router = TestBed.inject(Router);
    const result = TestBed.runInInjectionContext(() =>
      adminGuard({} as never, {} as never),
    );

    expect(result).toEqual(router.parseUrl('/login'));
  });

  it('allows administrators and rejects customers from admin routes', () => {
    const router = TestBed.inject(Router);
    currentUser.set({
      accessToken: 'admin-token',
      tokenType: 'Bearer',
      expiresIn: 1800,
      username: 'admin',
      role: 'ADMIN',
    });

    expect(TestBed.runInInjectionContext(() => adminGuard({} as never, {} as never))).toBe(true);

    currentUser.set({
      accessToken: 'customer-token',
      tokenType: 'Bearer',
      expiresIn: 1800,
      username: 'customer',
      role: 'CUSTOMER',
    });
    const result = TestBed.runInInjectionContext(() =>
      adminGuard({} as never, {} as never),
    );

    expect(result).toEqual(router.parseUrl('/loans'));
  });

  it('allows only customers into the customer loan area', () => {
    const router = TestBed.inject(Router);
    currentUser.set({
      accessToken: 'customer-token',
      tokenType: 'Bearer',
      expiresIn: 1800,
      username: 'customer',
      role: 'CUSTOMER',
    });

    expect(TestBed.runInInjectionContext(() => customerGuard({} as never, {} as never))).toBe(true);

    currentUser.set({
      accessToken: 'admin-token',
      tokenType: 'Bearer',
      expiresIn: 1800,
      username: 'admin',
      role: 'ADMIN',
    });
    const result = TestBed.runInInjectionContext(() =>
      customerGuard({} as never, {} as never),
    );

    expect(result).toEqual(router.parseUrl('/admin/loans'));
  });

  it('allows any authenticated role to visit the profile', () => {
    currentUser.set({
      accessToken: 'customer-token',
      tokenType: 'Bearer',
      expiresIn: 1800,
      username: 'customer',
      role: 'CUSTOMER',
    });

    expect(TestBed.runInInjectionContext(() =>
      authenticatedGuard({} as never, {} as never),
    )).toBe(true);
  });
});
