package com.logistica.sistema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record DevolucaoAtivoRequest(
        @NotBlank String codigoAtivo,
        @PositiveOrZero Integer kmChegada,
        String observacoes
) {
}
