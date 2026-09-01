-- V1: estrutura inicial - modelo generalizado de Ativo + Ponto
--
-- Decisao de modelagem: 'ativos' guarda os campos comuns a qualquer
-- bem controlavel (veiculo, ferramenta, EPI...). Cada tipo especifico
-- tem sua propria tabela de extensao (ativos_veiculo, ativos_epi),
-- referenciando ativos.id. Isso evita colunas nulas em excesso e
-- deixa facil adicionar um novo tipo de ativo no futuro sem alterar
-- a tabela principal.

CREATE TABLE usuarios (
    id              BIGSERIAL PRIMARY KEY,
    nome            VARCHAR(150) NOT NULL,
    cpf             VARCHAR(11) NOT NULL UNIQUE,
    matricula       VARCHAR(20) NOT NULL UNIQUE,
    cargo           VARCHAR(100),
    setor           VARCHAR(100),
    email           VARCHAR(150) NOT NULL UNIQUE,
    senha_hash      VARCHAR(255) NOT NULL,
    role            VARCHAR(20) NOT NULL DEFAULT 'FUNCIONARIO',
    ativo           BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em       TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE ativos (
    id              BIGSERIAL PRIMARY KEY,
    tipo            VARCHAR(20) NOT NULL,
    nome            VARCHAR(150) NOT NULL,
    codigo          VARCHAR(50) NOT NULL UNIQUE, -- ex: placa, numero de serie, codigo do QR code
    status          VARCHAR(20) NOT NULL DEFAULT 'DISPONIVEL',
    ativo           BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em       TIMESTAMP NOT NULL DEFAULT now()
);

-- Extensao especifica para ativos do tipo VEICULO
CREATE TABLE ativos_veiculo (
    ativo_id                BIGINT PRIMARY KEY REFERENCES ativos(id),
    placa                   VARCHAR(10) NOT NULL UNIQUE,
    marca                   VARCHAR(100),
    modelo                  VARCHAR(100) NOT NULL,
    km_atual                INTEGER NOT NULL DEFAULT 0,
    data_ultima_manutencao  DATE
);

-- Extensao especifica para ativos do tipo EPI
CREATE TABLE ativos_epi (
    ativo_id        BIGINT PRIMARY KEY REFERENCES ativos(id),
    numero_ca       VARCHAR(20),
    validade_ca     DATE
);

CREATE TABLE movimentacao_ativos (
    id                  BIGSERIAL PRIMARY KEY,
    ativo_id            BIGINT NOT NULL REFERENCES ativos(id),
    usuario_id          BIGINT NOT NULL REFERENCES usuarios(id),
    data_hora_retirada  TIMESTAMP NOT NULL DEFAULT now(),
    data_hora_devolucao TIMESTAMP,
    observacoes         TEXT,
    status              VARCHAR(20) NOT NULL DEFAULT 'ABERTO'
);

-- Um ativo so pode ter UMA movimentacao aberta por vez
CREATE UNIQUE INDEX idx_um_aberto_por_ativo
    ON movimentacao_ativos (ativo_id)
    WHERE status = 'ABERTO';

CREATE INDEX idx_movimentacao_usuario ON movimentacao_ativos (usuario_id);
CREATE INDEX idx_movimentacao_status ON movimentacao_ativos (status);

CREATE TABLE registro_ponto (
    id              BIGSERIAL PRIMARY KEY,
    usuario_id      BIGINT NOT NULL REFERENCES usuarios(id),
    data            DATE NOT NULL,
    hora_entrada    TIME,
    hora_saida      TIME,
    confirmado      BOOLEAN NOT NULL DEFAULT FALSE,
    criado_em       TIMESTAMP NOT NULL DEFAULT now(),
    -- um funcionario so tem um registro por dia
    UNIQUE (usuario_id, data)
);

CREATE INDEX idx_ponto_usuario_data ON registro_ponto (usuario_id, data);
