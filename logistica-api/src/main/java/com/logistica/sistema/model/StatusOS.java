package com.logistica.sistema.model;

/**
 * Status de uma Ordem de Servico. EM_PRODUCAO quando qualquer etapa
 * de qualquer serial inicia; CONCLUIDA quando todos os seriais
 * estiverem concluidos.
 */
public enum StatusOS {
    ABERTA,
    EM_PRODUCAO,
    CONCLUIDA,
    CANCELADA
}
