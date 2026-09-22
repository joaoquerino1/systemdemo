package com.logistica.sistema.model;

/**
 * Status de uma etapa do cronograma. PENDENTE = ainda nao iniciada;
 * EM_ANDAMENTO = iniciada (tem data de inicio); CONCLUIDA = finalizada
 * (tem data de conclusao e quantidade produzida).
 */
public enum StatusEtapa {
    PENDENTE,
    EM_ANDAMENTO,
    CONCLUIDA
}
