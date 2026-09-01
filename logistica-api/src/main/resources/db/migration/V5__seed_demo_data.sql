-- V5: Seed data for Bravial Demo - TechFrame Tecnologia Ltda
-- Fictional installation company with demo assets, employees, and history
--
-- Password for all users: demo12345 (BCrypt hash)

-- =============================================================================
-- USERS (password: demo12345, BCrypt hashed)
-- =============================================================================
INSERT INTO usuarios (nome, cpf, matricula, cargo, setor, email, senha_hash, role, ativo, criado_em) VALUES
('Carlos Mendes',    '12345678901', 'TF-0001', 'Gerente de Operacoes', 'Administracao', 'admin@techframe.com',    '$2b$10$qQmmcHY9OvnMApQF2KDlZeNUHl6UMD0mwkqE1gof8jVRgbZ/./i1.', 'ADMIN',    true, now()),
('Ana Beatriz Silva','23456789012', 'TF-0002', 'Supervisora de Obras',  'Operacoes',     'gestor@techframe.com',   '$2b$10$qQmmcHY9OvnMApQF2KDlZeNUHl6UMD0mwkqE1gof8jVRgbZ/./i1.', 'GESTOR',   true, now()),
('Joao Pedro Alves', '34567890123', 'TF-0003', 'Tecnico de Instalacao', 'Operacoes',     'joao@techframe.com',     '$2b$10$qQmmcHY9OvnMApQF2KDlZeNUHl6UMD0mwkqE1gof8jVRgbZ/./i1.', 'FUNCIONARIO', true, now()),
('Maria Fernanda',   '45678901234', 'TF-0004', 'Tecnica de Eletrica',   'Operacoes',     'maria@techframe.com',    '$2b$10$qQmmcHY9OvnMApQF2KDlZeNUHl6UMD0mwkqE1gof8jVRgbZ/./i1.', 'FUNCIONARIO', true, now()),
('Rafael Costa',     '56789012345', 'TF-0005', 'Motorista / Logistica', 'Logistica',     'rafael@techframe.com',   '$2b$10$qQmmcHY9OvnMApQF2KDlZeNUHl6UMD0mwkqE1gof8jVRgbZ/./i1.', 'FUNCIONARIO', true, now()),
('Luciana Santos',   '67890123456', 'TF-0006', 'Auxiliar Administrativo','Administracao', 'luciana@techframe.com',  '$2b$10$qQmmcHY9OvnMApQF2KDlZeNUHl6UMD0mwkqE1gof8jVRgbZ/./i1.', 'FUNCIONARIO', true, now());

-- =============================================================================
-- ASSETS (20 total: 4 vehicles, 8 tools, 5 PPE, 3 other)
-- =============================================================================
INSERT INTO ativos (tipo, nome, codigo, status, ativo, criado_em, version) VALUES
('VEICULO', 'Fiorino Branca - TF',    'VEH-001', 'EM_USO',     true, now(), 0),
('VEICULO', 'Hilux Prata - TF',       'VEH-002', 'DISPONIVEL', true, now(), 0),
('VEICULO', 'Saveiro Vermelha - TF',  'VEH-003', 'MANUTENCAO', true, now(), 0),
('VEICULO', 'Kombi Amarela - TF',     'VEH-004', 'DISPONIVEL', true, now(), 0);

INSERT INTO ativos_veiculo (ativo_id, placa, marca, modelo, km_atual, data_ultima_manutencao) VALUES
(1, 'ABC1D23', 'Ford',     'Fiorino 1.0 VCT-E',   45230, '2025-06-15'),
(2, 'DEF4E56', 'Toyota',   'Hilux SRV 2.8 CD 4x4', 78100, '2025-07-20'),
(3, 'GHI7F89', 'Volkswagen','Saveiro Cross 1.6',    32800, '2025-08-01'),
(4, 'JKL0G12', 'Volkswagen','Kombi 1.4 VHT-E',     112050, '2025-05-10');

INSERT INTO ativos (tipo, nome, codigo, status, ativo, criado_em, version) VALUES
('FERRAMENTA', 'Broca SDS-Plus 10mm',           'FER-001', 'EM_USO',     true, now(), 0),
('FERRAMENTA', 'Makita Serra Circular 7"',       'FER-002', 'DISPONIVEL', true, now(), 0),
('FERRAMENTA', 'Parafusadeira Bosch GSR 18V',    'FER-003', 'EM_USO',     true, now(), 0),
('FERRAMENTA', 'Nivel Laser Bosch GOL 32',       'FER-004', 'DISPONIVEL', true, now(), 0),
('FERRAMENTA', 'Trena Stanley 5m',               'FER-005', 'DISPONIVEL', true, now(), 0),
('FERRAMENTA', 'Alicate Universal 8"',           'FER-006', 'DISPONIVEL', true, now(), 0),
('FERRAMENTA', 'Chave Inglesa 12"',              'FER-007', 'MANUTENCAO', true, now(), 0),
('FERRAMENTA', 'Estilete Olfa com laminas',      'FER-008', 'DISPONIVEL', true, now(), 0);

INSERT INTO ativos (tipo, nome, codigo, status, ativo, criado_em, version) VALUES
('EPI', 'Capacete de Seguranca Branco',    'EPI-001', 'EM_USO',     true, now(), 0),
('EPI', 'Luva de Vaqueta Cano Curto',      'EPI-002', 'DISPONIVEL', true, now(), 0),
('EPI', 'Oculos de Protecao Amarelo',      'EPI-003', 'EM_USO',     true, now(), 0),
('EPI', 'Cinto Paraquedista 3 Pontos',     'EPI-004', 'DISPONIVEL', true, now(), 0),
('EPI', 'Calcado de Seguranca Marrom',     'EPI-005', 'DISPONIVEL', true, now(), 0);

INSERT INTO ativos_epi (ativo_id, numero_ca, validade_ca) VALUES
(9,  'CA 31.207', '2027-03-15'),
(10, 'CA 28.912', '2026-12-01'),
(11, 'CA 35.644', '2028-01-20'),
(12, 'CA 36.102', '2028-06-10'),
(13, 'CA 33.889', '2027-09-30');

INSERT INTO ativos (tipo, nome, codigo, status, ativo, criado_em, version) VALUES
('OUTRO', 'Compressor Atlas Copco 50L',        'OUT-001', 'DISPONIVEL', true, now(), 0),
('OUTRO', 'Gerador Eletrico 5kVA',              'OUT-002', 'EM_USO',     true, now(), 0),
('OUTRO', 'Extensao Eletrica 20m Bivolt',       'OUT-003', 'DISPONIVEL', true, now(), 0);

-- =============================================================================
-- ASSET MOVEMENTS
-- =============================================================================
INSERT INTO movimentacao_ativos (ativo_id, usuario_id, data_hora_retirada, data_hora_devolucao, observacoes, status, version) VALUES
(1, 3, '2025-08-01 08:00:00', '2025-08-01 17:30:00', 'Instalacao painel solar - Res. Silva', 'FECHADO', 0),
(2, 4, '2025-08-05 07:45:00', '2025-08-05 16:00:00', 'Reforma eletrica - Cond. Azul', 'FECHADO', 0),
(6, 3, '2025-07-28 09:00:00', '2025-07-28 15:00:00', 'Corte de tubulacao', 'FECHADO', 0),
(7, 4, '2025-07-30 08:30:00', '2025-07-30 12:00:00', 'Fixacao de quadro eletrico', 'FECHADO', 0),
(9, 3, '2025-07-15 07:00:00', '2025-07-15 17:00:00', 'Entrada em obra', 'FECHADO', 0),
(17, 5, '2025-08-02 08:15:00', '2025-08-02 16:45:00', 'Service de pintura', 'FECHADO', 0),
(1, 3, '2025-08-28 07:50:00', NULL, 'Instalacao fibra optica - Bairro Centro', 'ABERTO', 0),
(7, 4, '2025-08-29 08:20:00', NULL, 'Manutencao preventiva quadro de forca', 'ABERTO', 0),
(19, 5, '2025-08-30 09:00:00', NULL, 'Pintura externa - Edificio Central', 'ABERTO', 0),
(9, 3, '2025-08-28 07:50:00', NULL, 'Obra em andamento', 'ABERTO', 0),
(11, 4, '2025-08-29 08:20:00', NULL, 'Trabalho com eletrica', 'ABERTO', 0);

-- =============================================================================
-- TIME TRACKING (August 2025)
-- =============================================================================
INSERT INTO registro_ponto (usuario_id, data, hora_entrada, hora_saida_intervalo, hora_volta_intervalo, hora_saida, confirmado, criado_em) VALUES
-- Joao Pedro (id=3)
(3, '2025-08-01', '07:55', '12:00', '13:00', '17:30', true, now()),
(3, '2025-08-04', '07:50', '12:05', '13:00', '17:25', true, now()),
(3, '2025-08-05', '08:00', '12:00', '13:00', '17:35', true, now()),
(3, '2025-08-06', '07:45', '12:00', '13:10', '18:00', true, now()),
(3, '2025-08-07', '07:55', '12:00', '13:00', '17:20', true, now()),
(3, '2025-08-08', '08:00', '12:00', '13:00', '17:30', true, now()),
(3, '2025-08-11', '07:50', '12:05', '13:00', '17:25', true, now()),
(3, '2025-08-12', '07:55', '12:00', '13:00', '17:30', true, now()),
(3, '2025-08-13', '08:00', '12:00', '13:05', '18:10', true, now()),
(3, '2025-08-14', '07:45', '12:00', '13:00', '17:20', true, now()),
(3, '2025-08-15', '08:00', '12:00', '13:00', '17:00', true, now()),
(3, '2025-08-18', '07:50', '12:05', '13:00', '17:30', true, now()),
(3, '2025-08-19', '07:55', '12:00', '13:00', '17:35', true, now()),
(3, '2025-08-20', '08:00', '12:00', '13:00', '17:25', true, now()),
(3, '2025-08-21', '07:45', '12:00', '13:10', '18:00', true, now()),
(3, '2025-08-22', '08:00', '12:00', '13:00', '17:30', true, now()),
(3, '2025-08-25', '07:50', '12:05', '13:00', '17:25', true, now()),
(3, '2025-08-26', '07:55', '12:00', '13:00', '17:30', true, now()),
(3, '2025-08-27', '08:00', '12:00', '13:00', '17:35', true, now()),
(3, '2025-08-28', '07:50', '12:00', '13:00', '17:20', true, now()),
(3, '2025-08-29', '07:55', '12:05', '13:00', '17:30', true, now()),
-- Maria Fernanda (id=4)
(4, '2025-08-01', '08:00', '12:00', '13:00', '17:00', true, now()),
(4, '2025-08-04', '08:05', '12:00', '13:00', '17:00', true, now()),
(4, '2025-08-05', '07:50', '12:00', '13:05', '17:10', true, now()),
(4, '2025-08-06', '08:00', '12:00', '13:00', '17:00', true, now()),
(4, '2025-08-08', '08:00', '12:00', '13:00', '17:00', true, now()),
(4, '2025-08-11', '07:55', '12:00', '13:00', '17:00', true, now()),
(4, '2025-08-12', '08:00', '12:00', '13:00', '17:05', true, now()),
(4, '2025-08-13', '08:00', '12:00', '13:00', '17:00', true, now()),
(4, '2025-08-14', '08:00', '12:00', '13:00', '17:00', true, now()),
(4, '2025-08-15', '08:00', '12:00', '13:00', '17:00', true, now()),
(4, '2025-08-18', '08:00', '12:00', '13:00', '17:00', true, now()),
(4, '2025-08-19', '07:55', '12:05', '13:00', '17:00', true, now()),
(4, '2025-08-20', '08:00', '12:00', '13:00', '17:00', true, now()),
(4, '2025-08-21', '08:00', '12:00', '13:00', '17:05', true, now()),
(4, '2025-08-22', '08:00', '12:00', '13:00', '17:00', true, now()),
(4, '2025-08-25', '08:00', '12:00', '13:00', '17:00', true, now()),
(4, '2025-08-26', '08:05', '12:00', '13:00', '17:00', true, now()),
(4, '2025-08-27', '08:00', '12:00', '13:00', '17:00', true, now()),
(4, '2025-08-28', '08:00', '12:00', '13:00', '17:00', true, now()),
(4, '2025-08-29', '07:55', '12:00', '13:05', '17:00', true, now()),
-- Rafael Costa (id=5)
(5, '2025-08-01', '08:10', '12:00', '13:00', '17:00', true, now()),
(5, '2025-08-04', '08:05', '12:00', '13:00', '17:00', true, now()),
(5, '2025-08-05', '08:00', '12:00', '13:00', '17:00', true, now()),
(5, '2025-08-06', '08:15', '12:00', '13:00', '17:30', true, now()),
(5, '2025-08-07', '08:00', '12:00', '13:00', '17:00', true, now()),
(5, '2025-08-08', '08:05', '12:00', '13:00', '17:00', true, now()),
(5, '2025-08-11', '08:00', '12:00', '13:00', '17:00', true, now()),
(5, '2025-08-12', '08:10', '12:00', '13:00', '17:00', true, now()),
(5, '2025-08-13', '08:00', '12:00', '13:00', '17:00', true, now()),
(5, '2025-08-14', '08:00', '12:00', '13:00', '17:00', true, now()),
(5, '2025-08-15', '08:05', '12:00', '13:00', '17:00', true, now()),
(5, '2025-08-18', '08:00', '12:00', '13:00', '17:00', true, now()),
(5, '2025-08-19', '08:10', '12:00', '13:00', '17:00', true, now()),
(5, '2025-08-20', '08:00', '12:00', '13:00', '17:00', true, now()),
(5, '2025-08-21', '08:00', '12:00', '13:00', '17:00', true, now()),
(5, '2025-08-22', '08:05', '12:00', '13:00', '17:00', true, now()),
(5, '2025-08-25', '08:00', '12:00', '13:00', '17:00', true, now()),
(5, '2025-08-26', '08:00', '12:00', '13:00', '17:00', true, now()),
(5, '2025-08-27', '08:10', '12:00', '13:00', '17:00', true, now()),
(5, '2025-08-28', '08:00', '12:00', '13:00', '17:00', true, now()),
(5, '2025-08-29', '08:05', '12:00', '13:00', '17:00', true, now()),
-- Luciana Santos (id=6)
(6, '2025-08-01', '08:00', '12:00', '13:00', '17:00', true, now()),
(6, '2025-08-04', '08:00', '12:00', '13:00', '17:00', true, now()),
(6, '2025-08-05', '08:00', '12:00', '13:00', '17:00', true, now()),
(6, '2025-08-06', '08:00', '12:00', '13:00', '17:00', true, now()),
(6, '2025-08-07', '08:00', '12:00', '13:00', '17:00', true, now()),
(6, '2025-08-08', '08:00', '12:00', '13:00', '17:00', true, now()),
(6, '2025-08-11', '08:00', '12:00', '13:00', '17:00', true, now()),
(6, '2025-08-12', '08:00', '12:00', '13:00', '17:00', true, now()),
(6, '2025-08-13', '08:00', '12:00', '13:00', '17:00', true, now()),
(6, '2025-08-14', '08:00', '12:00', '13:00', '17:00', true, now()),
(6, '2025-08-15', '08:00', '12:00', '13:00', '17:00', true, now()),
(6, '2025-08-18', '08:00', '12:00', '13:00', '17:00', true, now()),
(6, '2025-08-19', '08:00', '12:00', '13:00', '17:00', true, now()),
(6, '2025-08-20', '08:00', '12:00', '13:00', '17:00', true, now()),
(6, '2025-08-21', '08:00', '12:00', '13:00', '17:00', true, now()),
(6, '2025-08-22', '08:00', '12:00', '13:00', '17:00', true, now()),
(6, '2025-08-25', '08:00', '12:00', '13:00', '17:00', true, now()),
(6, '2025-08-26', '08:00', '12:00', '13:00', '17:00', true, now()),
(6, '2025-08-27', '08:00', '12:00', '13:00', '17:00', true, now()),
(6, '2025-08-28', '08:00', '12:00', '13:00', '17:00', true, now()),
(6, '2025-08-29', '08:00', '12:00', '13:00', '17:00', true, now());

-- =============================================================================
-- AUDIT LOG
-- =============================================================================
INSERT INTO audit_log (acao, entidade, entidade_id, usuario_email, detalhes, criado_em) VALUES
('CRIACAO', 'ATIVO', 1, 'admin@techframe.com', 'Ativo: Fiorino Branca - TF (VEH-001) tipo=VEICULO', now() - interval '60 days'),
('CRIACAO', 'ATIVO', 5, 'admin@techframe.com', 'Ativo: Broca SDS-Plus 10mm (FER-001) tipo=FERRAMENTA', now() - interval '60 days'),
('CRIACAO', 'USUARIO', 1, 'sistema', 'Bootstrap admin: admin@techframe.com', now() - interval '60 days'),
('CRIACAO', 'USUARIO', 3, 'admin@techframe.com', 'Novo funcionario: Joao Pedro Alves (TF-003)', now() - interval '55 days'),
('RETIRADA', 'MOVIMENTACAO', 7, 'joao@techframe.com', 'Ativo: Fiorino Branca - TF (VEH-001) -> Joao Pedro Alves', now() - interval '3 days'),
('RETIRADA', 'MOVIMENTACAO', 8, 'maria@techframe.com', 'Ativo: Parafusadeira Bosch (FER-003) -> Maria Fernanda', now() - interval '2 days'),
('DEVOLUCAO', 'MOVIMENTACAO', 6, 'rafael@techframe.com', 'Ativo: Compressor Atlas Copco (OUT-001) devolvido', now() - interval '1 day');
