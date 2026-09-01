package com.logistica.sistema.dto;

import java.time.LocalDate;

public record DadosEpiRequest(
        String numeroCa,
        LocalDate validadeCa
) {
}
