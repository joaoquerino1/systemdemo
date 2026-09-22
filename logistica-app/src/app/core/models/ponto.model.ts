export interface RegistroPonto {
  id: number;
  usuarioNome: string;
  data: string; // ISO (yyyy-MM-dd) - dia comercial do registro
  // Marcacoes em ISO datetime (yyyy-MM-ddTHH:mm:ss), fuso de Brasilia
  horaEntrada: string | null;
  horaSaidaIntervalo: string | null;
  horaVoltaIntervalo: string | null;
  horaSaida: string | null;
  confirmado: boolean;
}
