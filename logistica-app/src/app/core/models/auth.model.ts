export type Role = 'ADMIN' | 'GESTOR' | 'FUNCIONARIO';

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface LoginResponse {
  token: string;
  id: number;
  nome: string;
  email: string;
  role: Role;
}

export interface UsuarioAutenticado {
  id: number;
  nome: string;
  email: string;
  role: Role;
}
