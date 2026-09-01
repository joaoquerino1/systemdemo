package com.logistica.sistema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

public record DadosVeiculoRequest(
        @NotBlank String placa,
        String marca,
        @NotBlank String modelo,
        @PositiveOrZero Integer kmAtual,
        LocalDate dataUltimaManutencao
) {
}
