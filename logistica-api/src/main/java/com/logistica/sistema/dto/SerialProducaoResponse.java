package com.logistica.sistema.dto;

import com.logistica.sistema.model.SerialProducao;
import com.logistica.sistema.model.StatusSerial;

import java.util.List;

public record SerialProducaoResponse(
        Long id,
        String codigoSerial,
        StatusSerial status,
        List<EtapaResponse> etapas
) {

    public static SerialProducaoResponse from(SerialProducao serial, List<EtapaResponse> etapas) {
        return new SerialProducaoResponse(
                serial.getId(), serial.getCodigoSerial(), serial.getStatus(), etapas);
    }
}
