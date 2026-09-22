# Plano — Cronograma de Produção (TechFrame)

> Projeção da feature baseada no formulário "CRONOGRAMA DE PRODUÇÃO"
> (doc "CONTROLE DE ORÇAMENTO - Página17"). Sem código ainda — este doc
> é para revisão antes de implementar.

## 1. Objetivo

Espelhar o cronograma de produção em papel no sistema: cada **OS** tem
**seriais**, e cada serial atravessa as 7 **etapas** de produção
(SEPARAÇÃO → CORTE → USINAGEM → PREPARAÇÃO → MONTAGEM → VIDROS →
CONFERÊNCIA), com colaborador responsável, quantidade produzida, datas
de início/conclusão e pendências.

## 2. Decisões já tomadas

| Decisão | Escolha |
|---|---|
| Ordem das etapas | **Livre** — qualquer etapa pode iniciar a qualquer momento; a restrição está na *conclusão* |
| Colaborador | **Usuário do sistema** (FK `usuarios.id`) — habilita relatórios de produtividade no futuro |
| CONFERÊNCIA | **Última etapa** — só conclui com as outras 6 concluídas |
| Reabrir etapa | **ADMIN/GESTOR podem reabrir** etapa concluída (auditado) |
| Orçamento | Não existe vínculo — "controle de orçamento" era erro de texto do PDF |
| Quantidade | **Obrigatória** (> 0) para concluir etapa |
| UI (frontend) | **Ng-zorro (ng-zorro-antd)** nas telas novas de Produção — compatível com Angular 18 e Material existente; Zard descartado (exige Tailwind v4 + CSS puro) |
| Entregável desta fase | Implementação completa (backend + frontend) |

## 3. Modelo de dados — `V6__create_producao.sql`

Seguindo as convenções da V1 (status VARCHAR, snake_case, índices
parciais) e da V3 (coluna `version` para optimistic locking):

```sql
-- V6: cronograma de producao - OS, seriais e etapas do cronograma
-- (espelha o formulario "CRONOGRAMA DE PRODUCAO")

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
    UNIQUE (serial_id, etapa)
);

CREATE INDEX idx_etapas_serial      ON cronograma_etapas (serial_id);
CREATE INDEX idx_etapas_colaborador ON cronograma_etapas (colaborador_id);
CREATE INDEX idx_seriais_os         ON seriais (ordem_servico_id);
```

Criação do serial **auto-popula as 7 etapas** (status PENDENTE), como
no formulário que já vem com as linhas impressas.

### Enums (pacote `model`)

- `EtapaProducao`: `SEPARACAO, CORTE, USINAGEM, PREPARACAO, MONTAGEM, VIDROS, CONFERENCIA`
- `StatusEtapa`: `PENDENTE, EM_ANDAMENTO, CONCLUIDA`
- `StatusSerial`: `PENDENTE, EM_PRODUCAO, CONCLUIDO, BLOQUEADO`
- `StatusOS`: `ABERTA, EM_PRODUCAO, CONCLUIDA, CANCELADA`

## 4. Backend — regras de negócio (`ProducaoService`)

Padrão `RegistroPontoService`: `@Transactional`, erros via
`RegraNegocioException`, respostas em records com `static from(...)`.

Regras (ordem livre, conclusão importa):

1. **Iniciar etapa** (`iniciar(id, colaboradorId)`):
   - etapa em `PENDENTE` (não permite reabrir por enquanto);
   - define `colaborador` (default: usuário autenticado) e
     `data_inicio = hoje`; status → `EM_ANDAMENTO`.
2. **Concluir etapa** (`concluir(id, quantidadeProduzida, pendencias?)`):
   - exige `data_inicio` preenchida e quantidade > 0;
   - define `data_conclusao = hoje`; status → `CONCLUIDA`;
   - se etapa = **CONFERENCIA**: exige as outras 6 concluídas
     (o rodapé do formulário: "conferir o material ao concluir a
     serial") → serial vai a `CONCLUIDO`;
   - qualquer conclusão move o serial para `EM_PRODUCAO` (se não
     concluído) e a OS para `EM_PRODUCAO`;
   - OS vai a `CONCLUIDA` quando todos os seriais estiverem `CONCLUIDO`.
3. **Pendências** (`registrarPendencia(id, texto)`): grava texto sem
   concluir a etapa — continua `EM_ANDAMENTO`.

### Endpoints — `ProducaoController` (`/api/producao`)

| Método | Rota | Permissão |
|---|---|---|
| `POST` | `/ordens` | ADMIN/GESTOR |
| `GET` | `/ordens?status=&page=` | autenticado |
| `GET` | `/ordens/{id}` | autenticado |
| `POST` | `/ordens/{id}/seriais` | ADMIN/GESTOR |
| `POST` | `/etapas/{id}/iniciar` | próprio colaborador ou ADMIN/GESTOR |
| `POST` | `/etapas/{id}/concluir` | próprio colaborador ou ADMIN/GESTOR |
| `PUT` | `/etapas/{id}/pendencias` | próprio colaborador ou ADMIN/GESTOR |

Auditoria das ações críticas via `AuditLogService` existente.

## 5. Frontend (Angular 18 + **ng-zorro-antd**, padrão `features/ativos`)

- `core/models/producao.model.ts` — interfaces + unions dos enums.
- `core/services/producao.service.ts` — espelha `PontoService`.
- `features/producao/lista/` — OS com tags de status (`nz-tag`) e filtro.
- `features/producao/detalhe/` — o formulário em tela: por serial,
  `nz-table` com as 7 etapas nas linhas e COLABORADOR / QTD / INÍCIO /
  CONCLUSÃO / PENDÊNCIAS nas colunas, com `nz-modal` para Iniciar /
  Concluir / Registrar pendência.
- `features/producao/formulario/` — nova OS com seriais dinâmicos
  (`nz-form` + `FormArray`).
- Rotas em `app.routes.ts` (`dashboard/producao*`) com `data.papeis`
  nas rotas de criação; item "Produção" no menu do shell.
- Telas antigas continuam em Material; coexistência é suportada
  (zorro usa próprio reset leve e prefixo `nz-`).

## 6. Testes

- E2E no estilo `FluxoCompletoE2ETest`: cria OS → inicia etapas fora de
  ordem → tenta concluir CONFERÊNCIA antes das demais (**deve falhar**)
  → conclui todas → serial e OS concluídos.
- Unicidades: etapa duplicada por serial, serial duplicado na OS,
  quantidade ≤ 0 na conclusão.

## 7. Fora de escopo desta fase

- Export do cronograma em PDF (segunda via impressa).
- Relatórios de produtividade por colaborador (o FK já prepara isso).
- Configuração de etapas por tipo de produto.
- Reabrir etapa concluída.

## 8. Pontos em aberto — RESOLVIDOS

1. **CONFERÊNCIA por último** — ✅ Sim, exige as outras 6 concluídas.
2. **Vínculo com orçamento** — ✅ Nenhum; era erro de texto do PDF.
3. **Desfazer conclusão** — ✅ ADMIN/GESTOR podem reabrir (auditado).
4. **Quantidade obrigatória** — ✅ Sim, > 0 para concluir.
5. **UI library** — ✅ ng-zorro-antd (Zard descartado: exige Tailwind v4/CSS puro, projeto usa v3/SCSS).
