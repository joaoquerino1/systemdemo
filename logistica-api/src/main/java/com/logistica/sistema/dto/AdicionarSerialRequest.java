package com.logistica.sistema.dto;

/**
 * Corpo do cadastro de um serial avulso em uma OS existente.
 */
public record AdicionarSerialRequest(
        String codigoSerial
) {
}
