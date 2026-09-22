package com.logistica.sistema.dto;

/**
 * Corpo do inicio de uma etapa. O colaboradorId e opcional: se nao
 * vier, assume o usuario autenticado. Enviar o id de OUTRO usuario
 * exige ADMIN/GESTOR (checado no controller, igual ao padrao do
 * folha-hora em PontoController).
 */
public record IniciarEtapaRequest(
        Long colaboradorId
) {
}
