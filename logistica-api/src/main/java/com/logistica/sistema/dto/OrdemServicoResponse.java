package com.logistica.sistema.dto;

import com.logistica.sistema.model.OrdemServico;
import com.logistica.sistema.model.StatusOS;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response de OS. Duas variantes: resumo (listagem, sem os seriais -
 * evita N+1) e detalhado (tela do cronograma, com seriais e etapas).
 */
public record OrdemServicoResponse(
        Long id,
        String numero,
        String descricao,
        StatusOS status,
        LocalDateTime criadoEm,
        Long totalSeriais,
        Long seriaisConcluidos,
        List<SerialProducaoResponse> seriais
) {

    /** Variante resumida para listagem (sem carregar seriais). */
    public static OrdemServicoResponse resumo(OrdemServico os) {
        return new OrdemServicoResponse(
                os.getId(), os.getNumero(), os.getDescricao(),
                os.getStatus(), os.getCriadoEm(), null, null, null);
    }

    /** Variante detalhada para a tela do cronograma. */
    public static OrdemServicoResponse detalhado(
            OrdemServico os, List<SerialProducaoResponse> seriais,
            long totalSeriais, long seriaisConcluidos) {
        return new OrdemServicoResponse(
                os.getId(), os.getNumero(), os.getDescricao(),
                os.getStatus(), os.getCriadoEm(),
                totalSeriais, seriaisConcluidos, seriais);
    }
}
