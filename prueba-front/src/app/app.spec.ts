import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from './core/auth/auth.service';
import { AuthResponse } from './core/auth/auth';
import { App } from './app';

describe('App', () => {
  const currentUser = signal<AuthResponse | null>(null);

  beforeEach(async () => {
    currentUser.set(null);
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: { currentUser, logout: () => currentUser.set(null) },
        },
      ],
    }).compileComponents();
  });

  it('renders the shared shell and router outlet', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.brand')?.textContent).toContain('Norte');
    expect(compiled.querySelector('router-outlet')).toBeTruthy();
    expect(compiled.textContent).toContain('FINANCIACIÓN A TU MEDIDA');
  });

  it('hides customer navigation from administrators', () => {
    currentUser.set({
      accessToken: 'admin-token',
      tokenType: 'Bearer',
      expiresIn: 1800,
      username: 'admin',
      role: 'ADMIN',
    });
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    const navigation = (fixture.nativeElement as HTMLElement).querySelector('.tabs');
    expect(navigation?.textContent).toContain('Préstamos');
    expect(navigation?.textContent).toContain('Usuarios');
    expect(navigation?.textContent).toContain('Mi perfil');
    expect(navigation?.textContent).not.toContain('Área de cliente');
  });

  it('shows customer navigation to customer accounts', () => {
    currentUser.set({
      accessToken: 'customer-token',
      tokenType: 'Bearer',
      expiresIn: 1800,
      username: 'customer',
      role: 'CUSTOMER',
    });
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    const navigation = (fixture.nativeElement as HTMLElement).querySelector('.tabs');
    expect(navigation?.textContent).toContain('Área de cliente');
    expect(navigation?.textContent).toContain('Mi perfil');
    expect(navigation?.textContent).not.toContain('Administración');
  });
});
