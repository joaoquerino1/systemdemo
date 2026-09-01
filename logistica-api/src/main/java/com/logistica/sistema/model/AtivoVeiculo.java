package com.logistica.sistema.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Atributos especificos de um Ativo do tipo VEICULO.
 * Compartilha a mesma chave primaria do Ativo (ativo_id),
 * padrao conhecido como "table per subtype".
 */
@Entity
@Table(name = "ativos_veiculo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtivoVeiculo {

    @Id
    @Column(name = "ativo_id")
    private Long ativoId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "ativo_id")
    private Ativo ativo;

    @Column(nullable = false, unique = true, length = 10)
    private String placa;

    private String marca;

    @Column(nullable = false, length = 100)
    private String modelo;

    @Column(name = "km_atual", nullable = false)
    @Builder.Default
    private Integer kmAtual = 0;

    @Column(name = "data_ultima_manutencao")
    private LocalDate dataUltimaManutencao;
}
