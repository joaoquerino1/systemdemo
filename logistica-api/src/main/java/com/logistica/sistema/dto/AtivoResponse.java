package com.logistica.sistema.dto;

import com.logistica.sistema.model.Ativo;
import com.logistica.sistema.model.StatusAtivo;
import com.logistica.sistema.model.TipoAtivo;

import java.time.LocalDateTime;

public record AtivoResponse(
        Long id,
        TipoAtivo tipo,
        String nome,
        String codigo,
        StatusAtivo status,
        boolean ativo,
        LocalDateTime criadoEm,
        DadosVeiculoResponse veiculo,
        DadosEpiResponse epi
) {
    public static AtivoResponse from(Ativo a, DadosVeiculoResponse veiculo, DadosEpiResponse epi) {
        return new AtivoResponse(
                a.getId(), a.getTipo(), a.getNome(), a.getCodigo(),
                a.getStatus(), a.isAtivo(), a.getCriadoEm(), veiculo, epi);
    }
}
