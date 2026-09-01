package com.logistica.sistema.dto;

import com.logistica.sistema.model.AtivoVeiculo;

import java.time.LocalDate;

public record DadosVeiculoResponse(
        String placa,
        String marca,
        String modelo,
        Integer kmAtual,
        LocalDate dataUltimaManutencao
) {
    public static DadosVeiculoResponse from(AtivoVeiculo v) {
        return new DadosVeiculoResponse(
                v.getPlaca(), v.getMarca(), v.getModelo(),
                v.getKmAtual(), v.getDataUltimaManutencao());
    }
}
