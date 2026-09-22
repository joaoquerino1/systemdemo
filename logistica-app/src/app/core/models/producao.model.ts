// Tipos do cronograma de producao (espelham os DTOs do backend).

export type StatusOS = 'ABERTA' | 'EM_PRODUCAO' | 'CONCLUIDA' | 'CANCELADA';
export type StatusSerial = 'PENDENTE' | 'EM_PRODUCAO' | 'CONCLUIDO' | 'BLOQUEADO';
export type StatusEtapa = 'PENDENTE' | 'EM_ANDAMENTO' | 'CONCLUIDA';
export type EtapaProducao =
  | 'SEPARACAO'
  | 'CORTE'
  | 'USINAGEM'
  | 'PREPARACAO'
  | 'MONTAGEM'
  | 'VIDROS'
  | 'CONFERENCIA';

export interface Etapa {
  id: number;
  etapa: EtapaProducao;
  etapaLabel: string;
  status: StatusEtapa;
  colaboradorId: number | null;
  colaboradorNome: string | null;
  quantidadeProduzida: number | null;
  dataInicio: string | null; // ISO datetime (yyyy-MM-ddTHH:mm:ss)
  dataConclusao: string | null;
  pendencias: string | null;
}

export interface SerialProducao {
  id: number;
  codigoSerial: string;
  status: StatusSerial;
  etapas: Etapa[];
}

export interface OrdemServico {
  id: number;
  numero: string;
  descricao: string | null;
  status: StatusOS;
  criadoEm: string;
  totalSeriais: number | null;
  seriaisConcluidos: number | null;
  seriais: SerialProducao[] | null;
}

export interface CriarOrdemServicoRequest {
  numero: string;
  descricao?: string;
  seriais: string[];
}

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
}
