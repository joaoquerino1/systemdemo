package com.logistica.sistema.exception;

/**
 * Lançada quando um recurso solicitado não existe.
 * Mapeada para HTTP 404 no exception handler global.
 */
public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String message) {
        super(message);
    }
}
