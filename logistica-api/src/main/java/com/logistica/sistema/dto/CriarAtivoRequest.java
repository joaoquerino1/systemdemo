package com.logistica.sistema.dto;

import com.logistica.sistema.model.TipoAtivo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

/**
 * 'veiculo' e obrigatorio quando tipo == VEICULO (validado no service,
 * nao aqui, porque a obrigatoriedade depende do valor de outro campo).
 * 'epi' e sempre opcional, mesmo quando tipo == EPI (numero do CA pode
 * ser cadastrado depois).
 */
public record CriarAtivoRequest(
        @NotNull TipoAtivo tipo,
        @NotBlank String nome,
        @NotBlank String codigo,
        @Valid DadosVeiculoRequest veiculo,
        @Valid DadosEpiRequest epi
) {
}
