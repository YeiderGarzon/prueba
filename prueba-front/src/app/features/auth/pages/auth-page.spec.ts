import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { authInterceptor } from '../../../core/auth/auth.interceptor';
import { AuthPage } from './auth-page';

describe('AuthPage', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AuthPage],
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    }).compileComponents();
  });

  it('shows sign in and registration actions', () => {
    const fixture = TestBed.createComponent(AuthPage);
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Inicia sesión');
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Regístrate');
  });

  it('asks for full name separately from username during registration', () => {
    const fixture = TestBed.createComponent(AuthPage);
    fixture.detectChanges();
    const registerButton = [...(fixture.nativeElement as HTMLElement).querySelectorAll('button')]
      .find((button) => button.textContent?.includes('Regístrate'));
    registerButton?.click();
    fixture.detectChanges();

    const fullName = (fixture.nativeElement as HTMLElement).querySelector('#full-name');
    const username = (fixture.nativeElement as HTMLElement).querySelector('#username');
    expect(fullName).toBeTruthy();
    expect(username).toBeTruthy();
    expect(fullName).not.toBe(username);
  });
});
