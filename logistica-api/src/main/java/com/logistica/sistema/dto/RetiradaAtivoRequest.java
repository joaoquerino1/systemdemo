package com.logistica.sistema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * kmSaida so e relevante quando o ativo retirado e um VEICULO;
 * para outros tipos, o service simplesmente ignora o campo.
 */
public record RetiradaAtivoRequest(
        @NotBlank String codigoAtivo,
        @NotNull Long usuarioId,
        @PositiveOrZero Integer kmSaida,
        String observacoes
) {
}
