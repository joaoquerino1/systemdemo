package com.logistica.sistema.dto;

import com.logistica.sistema.model.StatusAtivo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * O codigo do ativo (placa/serie/QR) nao e editavel por aqui de
 * proposito - trocar o identificador de um ativo ja em uso quebraria
 * o historico de movimentacoes. Se for realmente necessario, isso
 * deve ser uma operacao separada e auditada.
 */
public record AtualizarAtivoRequest(
        @NotBlank String nome,
        @NotNull StatusAtivo status,
        @Valid DadosVeiculoRequest veiculo,
        @Valid DadosEpiRequest epi
) {
}
