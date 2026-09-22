package com.logistica.sistema.dto;

/**
 * Corpo da conclusao de uma etapa. A quantidade produzida e
 * obrigatoria (> 0) - decisao confirmada no plano; pendencias fica
 * opcional para registrar observacoes finais da etapa.
 */
public record ConcluirEtapaRequest(
        Integer quantidadeProduzida,
        String pendencias
) {
}
