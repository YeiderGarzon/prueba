import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AdminUserUpdate, User, UserRole } from '../models/user';
import { UserApiService } from '../data-access/user-api.service';

@Component({
  imports: [FormsModule],
  selector: 'app-admin-users-page',
  templateUrl: './admin-users-page.html',
})
export class AdminUsersPage {
  private readonly api = inject(UserApiService);
  private readonly changeDetector = inject(ChangeDetectorRef);

  protected users: User[] = [];
  protected selected: User | null = null;
  protected username = '';
  protected fullName = '';
  protected role: UserRole = 'CUSTOMER';
  protected password = '';
  protected loading = true;
  protected saving = false;
  protected error = '';
  protected notice = '';

  constructor() {
    this.loadUsers();
  }

  protected edit(user: User): void {
    this.selected = user;
    this.username = user.username;
    this.fullName = user.fullName;
    this.role = user.role;
    this.password = '';
    this.error = '';
    this.notice = '';
  }

  protected cancel(): void {
    this.selected = null;
    this.password = '';
    this.error = '';
  }

  protected save(): void {
    if (!this.selected) return;
    this.error = '';
    this.notice = '';
    const request: AdminUserUpdate = {
      username: this.username.trim(),
      fullName: this.fullName.trim(),
      role: this.role,
      ...(this.password ? { password: this.password } : {}),
    };
    this.saving = true;
    this.api.update(this.selected.id, request).subscribe({
      next: (updated) => {
        this.users = this.users.map((user) => user.id === updated.id ? updated : user);
        this.selected = null;
        this.password = '';
        this.saving = false;
        this.notice = `Se actualizó el usuario ${updated.username}.`;
        this.changeDetector.markForCheck();
      },
      error: (error: HttpErrorResponse) => {
        this.saving = false;
        this.error = this.errorMessage(error);
        this.changeDetector.markForCheck();
      },
    });
  }

  private loadUsers(): void {
    this.api.list().subscribe({
      next: (users) => {
        this.users = users;
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

  private errorMessage(error: HttpErrorResponse): string {
    if (error.status === 0) return 'No se pudo conectar con el servidor.';
    if (error.status === 400) return 'Revisa el nombre de usuario y la contraseña (mínimo 12 caracteres).';
    if (error.status === 404) return 'El usuario ya no existe. Actualiza la lista e inténtalo de nuevo.';
    if (error.status === 409) return error.error?.message ?? 'No se pudo guardar el cambio por una regla de negocio.';
    return 'Ocurrió un error al procesar los usuarios.';
  }
}
