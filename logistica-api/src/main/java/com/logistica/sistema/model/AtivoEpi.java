package com.logistica.sistema.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Atributos especificos de um Ativo do tipo EPI.
 * O CA (Certificado de Aprovacao) tem validade - importante
 * para alertar quando o EPI precisa ser trocado/renovado.
 */
@Entity
@Table(name = "ativos_epi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtivoEpi {

    @Id
    @Column(name = "ativo_id")
    private Long ativoId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "ativo_id")
    private Ativo ativo;

    @Column(name = "numero_ca", length = 20)
    private String numeroCa;

    @Column(name = "validade_ca")
    private LocalDate validadeCa;
}
