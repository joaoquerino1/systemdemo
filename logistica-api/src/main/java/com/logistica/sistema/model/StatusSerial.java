package com.logistica.sistema.model;

/**
 * Status de um serial dentro de uma OS. BLOQUEADO fica reservado para
 * uso futuro (ex: pendencia critica impedindo producao).
 */
public enum StatusSerial {
    PENDENTE,
    EM_PRODUCAO,
    CONCLUIDO,
    BLOQUEADO
}
