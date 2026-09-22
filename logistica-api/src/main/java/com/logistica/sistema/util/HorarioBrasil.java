package com.logistica.sistema.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Relogio do negocio, fixado no fuso de Brasilia (UTC-3). Nao usar
 * LocalDate.now()/LocalTime.now() direto nos services: eles seguem o
 * timezone padrao da JVM, que no servidor (ex: Railway) e UTC - os
 * pontos batidos sairiam com 3h de diferenca do horario local.
 *
 * Se um dia cada usuario tiver um fuso proprio, trocar a implementacao
 * por um Clock injetado por usuario; por enquanto, horario base da
 * empresa e America/Sao_Paulo.
 */
public final class HorarioBrasil {

    public static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private HorarioBrasil() {
    }

    /** Data de hoje no fuso de Brasilia. */
    public static LocalDate hoje() {
        return LocalDate.now(FUSO);
    }

    /** Hora atual no fuso de Brasilia. */
    public static LocalTime agora() {
        return LocalTime.now(FUSO);
    }

    /** Data e hora completas no fuso de Brasilia. */
    public static LocalDateTime agoraCompleta() {
        return LocalDateTime.now(FUSO);
    }
}
