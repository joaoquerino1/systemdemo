package com.logistica.sistema.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Representa qualquer bem controlavel (veiculo, ferramenta, EPI, etc).
 * Campos especificos de cada tipo ficam em tabelas de extensao
 * (ver AtivoVeiculo, AtivoEpi), relacionadas 1-para-1 via ativo_id.
 */
@Entity
@Table(name = "ativos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ativo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoAtivo tipo;

    @Column(nullable = false, length = 150)
    private String nome;

    // codigo de identificacao unico: placa, numero de serie, ou codigo
    // gerado para o QR code do item
    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusAtivo status = StatusAtivo.DISPONIVEL;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Version
    private Long version;
}
