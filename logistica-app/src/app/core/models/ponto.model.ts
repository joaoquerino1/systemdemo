export interface RegistroPonto {
  id: number;
  usuarioNome: string;
  data: string; // ISO (yyyy-MM-dd)
  horaEntrada: string | null; // HH:mm:ss
  horaSaidaIntervalo: string | null;
  horaVoltaIntervalo: string | null;
  horaSaida: string | null;
  confirmado: boolean;
}
