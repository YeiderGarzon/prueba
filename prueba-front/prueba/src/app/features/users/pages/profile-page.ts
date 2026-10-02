import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { UserApiService } from '../data-access/user-api.service';

@Component({
  imports: [FormsModule],
  selector: 'app-profile-page',
  templateUrl: './profile-page.html',
})
export class ProfilePage {
  protected readonly auth = inject(AuthService);
  private readonly api = inject(UserApiService);
  private readonly changeDetector = inject(ChangeDetectorRef);
  private readonly router = inject(Router);

  protected username = this.auth.currentUser()?.username ?? '';
  protected fullName = '';
  protected password = '';
  protected loading = false;
  protected saving = false;
  protected error = '';
  protected notice = '';

  constructor() {
    this.loading = true;
    this.api.current().subscribe({
      next: (user) => {
        this.username = user.username;
        this.fullName = user.fullName;
        this.loading = false;
        this.changeDetector.markForCheck();
      },
      error: (error: HttpErrorResponse) => {
        this.loading = false;
        this.error = this.errorMessage(error);
        this.changeDetector.markForCheck();
      },
    });
  }

  protected save(): void {
    this.error = '';
    this.notice = '';
    const previousUsername = this.auth.currentUser()?.username;
    this.saving = true;
    this.api.updateCurrent({
      username: this.username.trim(),
      fullName: this.fullName.trim(),
      ...(this.password ? { password: this.password } : {}),
    }).subscribe({
      next: (user) => {
        this.saving = false;
        this.password = '';
        if (previousUsername !== user.username) {
          this.auth.logout();
          void this.router.navigateByUrl('/login');
          return;
        }
        this.notice = 'Tu perfil se actualizó correctamente.';
        this.changeDetector.markForCheck();
      },
      error: (error: HttpErrorResponse) => {
        this.saving = false;
        this.error = this.errorMessage(error);
        this.changeDetector.markForCheck();
      },
    });
  }

  private errorMessage(error: HttpErrorResponse): string {
    if (error.status === 0) return 'No se pudo conectar con el servidor.';
    if (error.status === 409) return 'Ese nombre de usuario ya está en uso.';
    if (error.status === 400) return 'Verifica el usuario y que la nueva contraseña tenga al menos 12 caracteres.';
    return 'No se pudo actualizar el perfil.';
  }
}
