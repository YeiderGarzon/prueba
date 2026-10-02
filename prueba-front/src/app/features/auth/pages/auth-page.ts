import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  imports: [FormsModule],
  selector: 'app-auth-page',
  templateUrl: './auth-page.html',
})
export class AuthPage {
  protected readonly auth = inject(AuthService);
  private readonly changeDetector = inject(ChangeDetectorRef);
  private readonly router = inject(Router);

  protected authMode: 'login' | 'register' = 'login';
  protected username = '';
  protected fullName = '';
  protected password = '';
  protected authenticating = false;
  protected error = '';

  protected setAuthMode(mode: 'login' | 'register'): void {
    this.authMode = mode;
    this.error = '';
    this.fullName = '';
  }

  protected authenticate(): void {
    this.error = '';
    const credentials = { username: this.username.trim(), password: this.password };
    const fullName = this.fullName.trim();
    if (!credentials.username || !credentials.password
        || (this.authMode === 'register' && !fullName)) {
      this.error = this.authMode === 'register'
        ? 'Ingresa tu nombre completo, usuario y contraseña.'
        : 'Ingresa tu usuario y contraseña.';
      return;
    }

    this.authenticating = true;
    const authenticated = () => {
      this.authenticating = false;
      this.password = '';
      this.changeDetector.markForCheck();
      void this.router.navigateByUrl(
        this.auth.currentUser()?.role === 'ADMIN' ? '/admin/loans' : '/loans',
      );
    };
    const failed = (error: HttpErrorResponse) => {
      this.authenticating = false;
      this.error = this.errorMessage(error);
      this.changeDetector.markForCheck();
    };

    if (this.authMode === 'register') {
      this.auth.register({ ...credentials, fullName }).subscribe({
        next: () => this.auth.login(credentials).subscribe({
          next: authenticated,
          error: failed,
        }),
        error: failed,
      });
      return;
    }
    this.auth.login(credentials).subscribe({
      next: authenticated,
      error: failed,
    });
  }

  private errorMessage(error: HttpErrorResponse): string {
    if (error.status === 0) {
      return 'No se pudo conectar con la API. Verifica que el backend esté activo en localhost:8080.';
    }
    if (error.status === 400) {
      return this.authMode === 'register'
        ? 'Ingresa un nombre completo (máximo 120 caracteres), un usuario de 3 a 40 caracteres y una contraseña de al menos 12 caracteres.'
        : 'Los datos no son válidos.';
    }
    if (error.status === 401) {
      return 'Usuario o contraseña incorrectos.';
    }
    if (error.status === 409) {
      return 'Ese nombre de usuario ya está registrado.';
    }
    return error.error?.detail ?? 'Ocurrió un error al procesar la solicitud.';
  }
}
