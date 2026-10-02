export type UserRole = 'CUSTOMER' | 'ADMIN';

export interface AuthResponse {
  accessToken: string;
  tokenType: 'Bearer';
  expiresIn: number;
  username: string;
  role: UserRole;
}

export interface Credentials {
  username: string;
  password: string;
}

export interface RegistrationDetails extends Credentials {
  fullName: string;
}
