package com.logistica.sistema.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String acao;

    @Column(nullable = false, length = 50)
    private String entidade;

    private Long entidadeId;

    @Column(length = 100)
    private String usuarioEmail;

    @Column(columnDefinition = "TEXT")
    private String detalhes;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();
}
