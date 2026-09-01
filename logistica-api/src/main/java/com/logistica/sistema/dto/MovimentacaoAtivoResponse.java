package com.logistica.sistema.dto;

import com.logistica.sistema.model.MovimentacaoAtivo;
import com.logistica.sistema.model.StatusMovimentacao;

import java.time.LocalDateTime;

public record MovimentacaoAtivoResponse(
        Long id,
        String ativoNome,
        String ativoCodigo,
        String usuarioNome,
        LocalDateTime dataHoraRetirada,
        LocalDateTime dataHoraDevolucao,
        StatusMovimentacao status,
        String observacoes
) {
    public static MovimentacaoAtivoResponse from(MovimentacaoAtivo m) {
        return new MovimentacaoAtivoResponse(
                m.getId(),
                m.getAtivo().getNome(),
                m.getAtivo().getCodigo(),
                m.getUsuario().getNome(),
                m.getDataHoraRetirada(),
                m.getDataHoraDevolucao(),
                m.getStatus(),
                m.getObservacoes()
        );
    }
}
