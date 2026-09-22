-- V8: conversao das marcacoes de ponto de TIME para TIMESTAMP e das
-- datas de etapa de producao de DATE para TIMESTAMP.
--
-- Por que TIMESTAMP: preserva o instante completo do batimento (data +
-- hora), alem de registrar eventos que atravessam a meia-noite sem
-- ambiguidade. A coluna registro_ponto.data permanece DATE - ela
-- representa o dia comercial e sustenta a unicidade diaria do ponto.
--
-- Estrategia: reconstruir o timestamp combinando a data existente do
-- registro com a hora marcada; etapas usam a propria data de
-- inicio/conclusao a meia-noite. Sem zona explicita: o relogio de
-- negocio e Brasilia (HorarioBrasil), alinhado ao TIMESTAMP WITHOUT
-- TIME ZONE do Postgres.

ALTER TABLE registro_ponto
    ADD COLUMN hora_entrada_ts      TIMESTAMP,
    ADD COLUMN hora_saida_intervalo_ts TIMESTAMP,
    ADD COLUMN hora_volta_intervalo_ts TIMESTAMP,
    ADD COLUMN hora_saida_ts        TIMESTAMP;

UPDATE registro_ponto
SET hora_entrada_ts = data + hora_entrada,
    hora_saida_intervalo_ts = data + hora_saida_intervalo,
    hora_volta_intervalo_ts = data + hora_volta_intervalo,
    hora_saida_ts = data + hora_saida;

ALTER TABLE registro_ponto DROP COLUMN hora_entrada;
ALTER TABLE registro_ponto DROP COLUMN hora_saida_intervalo;
ALTER TABLE registro_ponto DROP COLUMN hora_volta_intervalo;
ALTER TABLE registro_ponto DROP COLUMN hora_saida;

ALTER TABLE registro_ponto RENAME COLUMN hora_entrada_ts TO hora_entrada;
ALTER TABLE registro_ponto RENAME COLUMN hora_saida_intervalo_ts TO hora_saida_intervalo;
ALTER TABLE registro_ponto RENAME COLUMN hora_volta_intervalo_ts TO hora_volta_intervalo;
ALTER TABLE registro_ponto RENAME COLUMN hora_saida_ts TO hora_saida;

ALTER TABLE cronograma_etapas
    ADD COLUMN data_inicio_ts      TIMESTAMP,
    ADD COLUMN data_conclusao_ts   TIMESTAMP;

UPDATE cronograma_etapas
SET data_inicio_ts = data_inicio,
    data_conclusao_ts = data_conclusao;

ALTER TABLE cronograma_etapas DROP COLUMN data_inicio;
ALTER TABLE cronograma_etapas DROP COLUMN data_conclusao;

ALTER TABLE cronograma_etapas RENAME COLUMN data_inicio_ts TO data_inicio;
ALTER TABLE cronograma_etapas RENAME COLUMN data_conclusao_ts TO data_conclusao;
