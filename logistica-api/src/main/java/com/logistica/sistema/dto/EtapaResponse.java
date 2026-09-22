package com.logistica.sistema.dto;

import com.logistica.sistema.model.CronogramaEtapa;
import com.logistica.sistema.model.EtapaProducao;
import com.logistica.sistema.model.StatusEtapa;

import java.time.LocalDateTime;

/**
 * Uma linha do cronograma (espelho da linha do formulario impresso):
 * etapa + colaborador + quantidade + datas + pendencias.
 */
public record EtapaResponse(
        Long id,
        EtapaProducao etapa,
        String etapaLabel,
        StatusEtapa status,
        Long colaboradorId,
        String colaboradorNome,
        Integer quantidadeProduzida,
        LocalDateTime dataInicio,
        LocalDateTime dataConclusao,
        String pendencias
) {

    public static EtapaResponse from(CronogramaEtapa e) {
        return new EtapaResponse(
                e.getId(),
                e.getEtapa(),
                e.getEtapa().getLabel(),
                e.getStatus(),
                e.getColaborador() != null ? e.getColaborador().getId() : null,
                e.getColaborador() != null ? e.getColaborador().getNome() : null,
                e.getQuantidadeProduzida(),
                e.getDataInicio(),
                e.getDataConclusao(),
                e.getPendencias());
    }
}
