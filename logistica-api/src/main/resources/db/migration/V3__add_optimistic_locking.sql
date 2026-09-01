-- V3: adiciona coluna 'version' para optimistic locking (JUnit @Version)
-- nas tabelas que sofrem atualizacoes concorrentes.

ALTER TABLE usuarios
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE ativos
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE movimentacao_ativos
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
