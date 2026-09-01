-- V2: adiciona os horarios de saida/volta do intervalo intrajornada
-- (almoco/descanso), necessarios para a folha de hora seguir o padrao
-- exigido (entrada, saida/retorno do intervalo, saida final).

ALTER TABLE registro_ponto
    ADD COLUMN hora_saida_intervalo TIME,
    ADD COLUMN hora_volta_intervalo TIME;
