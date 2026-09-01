package com.logistica.sistema.dto;

import com.logistica.sistema.model.RegistroPonto;

import java.time.LocalDate;
import java.time.LocalTime;

public record RegistroPontoResponse(
        Long id,
        String usuarioNome,
        LocalDate data,
        LocalTime horaEntrada,
        LocalTime horaSaidaIntervalo,
        LocalTime horaVoltaIntervalo,
        LocalTime horaSaida,
        boolean confirmado
) {
    public static RegistroPontoResponse from(RegistroPonto r) {
        return new RegistroPontoResponse(
                r.getId(), r.getUsuario().getNome(), r.getData(),
                r.getHoraEntrada(), r.getHoraSaidaIntervalo(), r.getHoraVoltaIntervalo(),
                r.getHoraSaida(), r.isConfirmado());
    }
}
