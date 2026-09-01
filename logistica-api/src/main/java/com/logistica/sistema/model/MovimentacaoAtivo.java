package com.logistica.sistema.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Representa um ciclo de retirada -> devolucao de qualquer Ativo
 * (veiculo, ferramenta, EPI...). Enquanto dataHoraDevolucao for nula,
 * a movimentacao esta ABERTA e o ativo esta EM_USO.
 *
 * Nota: atributos especificos do momento da retirada/devolucao que
 * so fazem sentido para um tipo (ex: km_saida/km_chegada de veiculo)
 * NAO ficam aqui - ficam refletidos na tabela de extensao do ativo
 * (ex: AtivoVeiculo.kmAtual e atualizado pelo service ao registrar
 * a movimentacao). Isso mantem esta entidade genuinamente generica.
 */
@Entity
@Table(name = "movimentacao_ativos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimentacaoAtivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ativo_id", nullable = false)
    private Ativo ativo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "data_hora_retirada", nullable = false)
    @Builder.Default
    private LocalDateTime dataHoraRetirada = LocalDateTime.now();

    @Column(name = "data_hora_devolucao")
    private LocalDateTime dataHoraDevolucao;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusMovimentacao status = StatusMovimentacao.ABERTO;
}
