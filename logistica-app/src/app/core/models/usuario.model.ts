import { Role } from './auth.model';

export interface Usuario {
  id: number;
  nome: string;
  email: string;
  cargo: string | null;
  setor: string | null;
  role: Role;
  ativo: boolean;
}

export interface CriarUsuarioRequest {
  nome: string;
  cpf: string;
  matricula: string;
  cargo?: string;
  setor?: string;
  email: string;
  senha: string;
  role?: Role;
}
