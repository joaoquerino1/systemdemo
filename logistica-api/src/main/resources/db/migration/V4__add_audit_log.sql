-- V4: adiciona tabela de auditoria para rastrear acoes importantes
-- (criacao/edicao de ativos, movimentacoes, criacao de usuarios, etc.)

CREATE TABLE audit_log (
    id              BIGSERIAL PRIMARY KEY,
    acao            VARCHAR(100) NOT NULL,
    entidade        VARCHAR(50) NOT NULL,
    entidade_id     BIGINT,
    usuario_email   VARCHAR(100),
    detalhes        TEXT,
    criado_em       TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_entidade ON audit_log (entidade, entidade_id);
CREATE INDEX idx_audit_data ON audit_log (criado_em DESC);
