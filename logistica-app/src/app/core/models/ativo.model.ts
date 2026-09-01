export type TipoAtivo = 'VEICULO' | 'FERRAMENTA' | 'EPI' | 'OUTRO';
export type StatusAtivo = 'DISPONIVEL' | 'EM_USO' | 'MANUTENCAO' | 'INATIVO';
export type StatusMovimentacao = 'ABERTO' | 'FECHADO' | 'ATRASADO';

export interface DadosVeiculo {
  placa: string;
  marca?: string;
  modelo: string;
  kmAtual?: number;
  dataUltimaManutencao?: string; // formato ISO (yyyy-MM-dd)
}

export interface DadosEpi {
  numeroCa?: string;
  validadeCa?: string; // formato ISO (yyyy-MM-dd)
}

export interface Ativo {
  id: number;
  tipo: TipoAtivo;
  nome: string;
  codigo: string;
  status: StatusAtivo;
  ativo: boolean;
  criadoEm: string;
  veiculo: DadosVeiculo | null;
  epi: DadosEpi | null;
}

export interface CriarAtivoRequest {
  tipo: TipoAtivo;
  nome: string;
  codigo: string;
  veiculo?: DadosVeiculo;
  epi?: DadosEpi;
}

export interface AtualizarAtivoRequest {
  nome: string;
  status: StatusAtivo;
  veiculo?: DadosVeiculo;
  epi?: DadosEpi;
}

export interface RetiradaAtivoRequest {
  codigoAtivo: string;
  usuarioId: number;
  kmSaida?: number;
  observacoes?: string;
}

export interface DevolucaoAtivoRequest {
  codigoAtivo: string;
  kmChegada?: number;
  observacoes?: string;
}

export interface MovimentacaoAtivo {
  id: number;
  ativoNome: string;
  ativoCodigo: string;
  usuarioNome: string;
  dataHoraRetirada: string;
  dataHoraDevolucao: string | null;
  status: StatusMovimentacao;
  observacoes: string | null;
}
