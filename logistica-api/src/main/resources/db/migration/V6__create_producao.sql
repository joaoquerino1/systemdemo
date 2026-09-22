-- V6: cronograma de producao - espelha o formulario "CRONOGRAMA DE
-- PRODUCAO" (OS -> seriais -> 7 etapas por serial, com colaborador,
-- quantidade produzida, datas e pendencias).
--
-- Decisoes de modelagem (doc docs/plano-cronograma-producao.md):
-- - a criacao do serial auto-popula as 7 etapas (como as linhas ja
--   impressas do formulario);
-- - ordem das etapas e LIVRE: a unica regra de sequencia (conferencia
--   por ultimo) vive no service, nao no banco;
-- - colaborador e FK de usuarios (relatorios de produtividade depois).

CREATE TABLE ordens_servico (
    id          BIGSERIAL PRIMARY KEY,
    numero      VARCHAR(30) NOT NULL UNIQUE,
    descricao   VARCHAR(255),
    status      VARCHAR(20) NOT NULL DEFAULT 'ABERTA',
    criado_em   TIMESTAMP NOT NULL DEFAULT now(),
    version     BIGINT
);

CREATE TABLE seriais (
    id               BIGSERIAL PRIMARY KEY,
    ordem_servico_id BIGINT NOT NULL REFERENCES ordens_servico(id),
    codigo_serial    VARCHAR(50) NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    criado_em        TIMESTAMP NOT NULL DEFAULT now(),
    version          BIGINT,
    -- um serial nao se repete dentro da mesma OS
    UNIQUE (ordem_servico_id, codigo_serial)
);

CREATE TABLE cronograma_etapas (
    id                   BIGSERIAL PRIMARY KEY,
    serial_id            BIGINT NOT NULL REFERENCES seriais(id),
    etapa                VARCHAR(20) NOT NULL, -- SEPARACAO..CONFERENCIA
    colaborador_id       BIGINT REFERENCES usuarios(id),
    quantidade_produzida INTEGER,
    data_inicio          DATE,
    data_conclusao       DATE,
    pendencias           TEXT,
    criado_em            TIMESTAMP NOT NULL DEFAULT now(),
    -- uma etapa aparece uma unica vez por serial (7 linhas fixas)
    UNIQUE (serial_id, etapa)
);

CREATE INDEX idx_etapas_serial      ON cronograma_etapas (serial_id);
CREATE INDEX idx_etapas_colaborador ON cronograma_etapas (colaborador_id);
CREATE INDEX idx_seriais_os         ON seriais (ordem_servico_id);
CREATE INDEX idx_os_status          ON ordens_servico (status);
