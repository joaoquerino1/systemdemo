package com.logistica.sistema.dto;

import java.util.List;

/**
 * Corpo da criacao de uma OS. A lista de seriais e obrigatoria - nao
 * faz sentido abrir uma OS de producao sem nenhum serial a produzir.
 */
public record CriarOrdemServicoRequest(
        String numero,
        String descricao,
        List<String> seriais
) {
}
