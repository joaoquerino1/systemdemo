package com.logistica.sistema.model;

/**
 * Define qual tabela de extensao consultar para detalhes especificos
 * do ativo (ex: VEICULO -> ativos_veiculo, EPI -> ativos_epi).
 */
public enum TipoAtivo {
    VEICULO,
    FERRAMENTA,
    EPI,
    OUTRO
}
