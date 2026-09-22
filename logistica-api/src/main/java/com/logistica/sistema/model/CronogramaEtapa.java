package com.logistica.sistema.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Uma linha do cronograma: uma etapa fixa de um serial. O colaborador
 * e um usuario do sistema (FK usuarios) para permitir relatorios de
 * produtividade no futuro. Ordem das etapas e livre - a unica regra
 * de sequencia (CONFERENCIA por ultimo) vive no ProducaoService.
 */
@Entity
@Table(name = "cronograma_etapas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CronogramaEtapa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "serial_id", nullable = false)
    private SerialProducao serial;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EtapaProducao etapa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colaborador_id")
    private Usuario colaborador;

    @Column(name = "quantidade_produzida")
    private Integer quantidadeProduzida;

    // TIMESTAMP (instante completo) em vez de DATE: preserva a hora
    // exata de inicio/conclusao de cada etapa.
    @Column(name = "data_inicio")
    private LocalDateTime dataInicio;

    @Column(name = "data_conclusao")
    private LocalDateTime dataConclusao;

    @Column(columnDefinition = "TEXT")
    private String pendencias;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusEtapa status = StatusEtapa.PENDENTE;

    @Column(name = "criado_em", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();
}
