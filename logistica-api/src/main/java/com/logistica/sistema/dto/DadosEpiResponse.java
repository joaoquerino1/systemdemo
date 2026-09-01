package com.logistica.sistema.dto;

import com.logistica.sistema.model.AtivoEpi;

import java.time.LocalDate;

public record DadosEpiResponse(
        String numeroCa,
        LocalDate validadeCa
) {
    public static DadosEpiResponse from(AtivoEpi e) {
        return new DadosEpiResponse(e.getNumeroCa(), e.getValidadeCa());
    }
}
