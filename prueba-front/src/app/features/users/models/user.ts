export type UserRole = 'CUSTOMER' | 'ADMIN';

export interface User {
  id: number;
  username: string;
  fullName: string;
  role: UserRole;
}

export interface ProfileUpdate {
  username: string;
  fullName: string;
  password?: string;
}

export interface AdminUserUpdate {
  username: string;
  fullName: string;
  role: UserRole;
  password?: string;
}
