package com.logistica.sistema.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Registro diario de ponto de um funcionario. Batido individualmente
 * ao chegar/sair da empresa, sem geolocalizacao ou foto por enquanto -
 * apenas uma confirmacao (equivalente a uma assinatura previamente
 * preenchida).
 *
 * Restricao de unicidade (usuario_id, data) no banco garante que so
 * exista um registro por funcionario por dia.
 */
@Entity
@Table(name = "registro_ponto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroPonto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDate data;

    // Marcacoes como TIMESTAMP (data + hora do instante do batimento).
    // A coluna data permanece DATE: dia comercial, base da unicidade.
    @Column(name = "hora_entrada")
    private LocalDateTime horaEntrada;

    @Column(name = "hora_saida_intervalo")
    private LocalDateTime horaSaidaIntervalo;

    @Column(name = "hora_volta_intervalo")
    private LocalDateTime horaVoltaIntervalo;

    @Column(name = "hora_saida")
    private LocalDateTime horaSaida;

    // Confirmacao do funcionario ao bater o ponto (equivale a
    // uma assinatura previamente preenchida)
    @Column(nullable = false)
    @Builder.Default
    private boolean confirmado = false;

    @Column(name = "criado_em", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();
}
