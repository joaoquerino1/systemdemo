package com.logistica.sistema.exception;

/**
 * Lançada quando uma operação viola uma regra de negócio
 * (ex: tentar retirar um ativo que já está em uso).
 * Mapeada para HTTP 409 (Conflict) no exception handler global.
 */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String message) {
        super(message);
    }
}
