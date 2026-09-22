package com.logistica.sistema.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Serial de producao dentro de uma OS. Cada serial criado nasce com
 * as 7 etapas do cronograma ja registradas (status PENDENTE),
 * espelhando as linhas impressas do formulario.
 */
@Entity
@Table(name = "seriais")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SerialProducao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServico ordemServico;

    @Column(name = "codigo_serial", nullable = false, length = 50)
    private String codigoSerial;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusSerial status = StatusSerial.PENDENTE;

    @OneToMany(mappedBy = "serial", fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    @Builder.Default
    private List<CronogramaEtapa> etapas = new ArrayList<>();

    @Column(name = "criado_em", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Version
    private Long version;
}
