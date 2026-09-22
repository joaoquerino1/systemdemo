-- V7: coluna status da etapa do cronograma (PENDENTE / EM_ANDAMENTO /
-- CONCLUIDA), faltante na V6 - a entidade CronogramaEtapa a exige e o
-- schema-validator bloqueia a subida da aplicacao sem ela.

ALTER TABLE cronograma_etapas
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE';
