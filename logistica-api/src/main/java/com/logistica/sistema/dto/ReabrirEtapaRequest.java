package com.logistica.sistema.dto;

/**
 * Corpo da reabertura de uma etapa concluida (so ADMIN/GESTOR). O
 * motivo e obrigatorio e vai para o log de auditoria.
 */
public record ReabrirEtapaRequest(
        String motivo
) {
}
