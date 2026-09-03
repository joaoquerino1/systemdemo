# System Demo

**Demo publica do Sistema de Gestão de Ativos e Inventarios** - Sistema logistico interno para gestao de ativos, controle de ponto e folha de hora.

> Este e um repositorio de demonstracao com dados ficticios da empresa **TechFrame Tecnologia Ltda**.
> Mantem a mesma arquitetura do sistema de producao, mas com dados seguros para exposicao publica.

**Stack:** Spring Boot 3.3 / Angular 18 / PostgreSQL 16 / Docker / Chart.js

---

## Credenciais de Demonstracao

| Funcao | Email | Senha |
|--------|-------|-------|
| Administrador | `admin@techframe.com` | `demo12345` |
| Gestor | `gestor@techframe.com` | `demo12345` |
| Funcionario | `joao@techframe.com` | `demo12345` |

---

## O Problema de Negocio

A **TechFrame Tecnologia Ltda** e uma empresa de instalacoes eletricas e fibra optica que enfrentava os seguintes desafios:

- **Perda de ferramentas e EPIs:** Sem controle de retirada/devolucao, equipamentos sumiam das obras
- **Frota descontrolada:** Veiculos eram usados sem registro, manutencao atrasada passava despercebida
- **Ponto manual:** Controle de jornada em planilha, sujeito a erros e fraudes
- **Folha de hora demorada:** Geracao manual do documento legal exigido por lei

O SystemDemo resolve tudo isso em um sistema unico, acessivel via web e mobile.

---

## Funcionalidades

### Gestao de Ativos
- CRUD completo com tipos: **Veiculo**, **Ferramenta**, **EPI**, **Outro**
- Cada tipo tem dados especificos (placa/km para veiculos, CA/validade para EPIs)
- **Retirada/Devolucao** com validacao de status e registro de responsavel
- Dashboard com metricas em tempo real

### Controle de Ponto
- Registro de entrada, saida para intervalo, volta do intervalo e saida final
- Timeline visual do ponto do dia
- Historico mensal com tabela completa

### Folha de Hora
- Geracao de PDF com marcacoes de ponto (obrigatorio por lei)
- Periodo selecionavel
- Acesso administrativo para gerar folhas de outros funcionarios

### Dashboard com Graficos
- Grafico de rosca: Status dos ativos (Disponivel, Em Uso, Manutencao)
- Grafico de barras: Ativos por tipo
- Grafico de linhas: Horas trabalhadas por funcionario

---

## Arquitetura

```
systemdemo/
├── logistica-api/              # Backend Spring Boot
│   ├── src/main/java/.../
│   │   ├── controller/         # REST controllers
│   │   ├── dto/                # Request/Response records
│   │   ├── model/              # JPA entities
│   │   ├── repository/         # Spring Data repositories
│   │   ├── security/           # JWT, auth, rate limiting
│   │   ├── service/            # Business logic + audit logging
│   │   └── exception/          # Global error handling
│   └── src/main/resources/
│       ├── db/migration/       # Flyway SQL migrations (V1-V5)
│       │   └── V5__seed_demo_data.sql  # Dados de demonstracao
│       └── application.yml     # Configuracao
│
├── logistica-app/              # Frontend Angular 18 (PWA)
│   └── src/app/
│       ├── core/               # Services, interceptors, guards, models
│       ├── features/           # Auth, Ativos, Ponto, FolhaHora, Funcionarios
│       └── shared/             # Layout shell (responsive sidebar)
│
├── docker-compose.yml          # Ambiente completo (DB + API + Frontend)
└── README.md
```

### Decisoes Tecnicas

- **Schema separado (`producao`)**: Isola dados de producao de testes
- **JWT stateless**: Autenticacao sem sessao, ideal para APIs REST
- **Optimistic Locking**: Previne atualizacoes concorrentes com `@Version`
- **Flyway**: Migracoes versionadas do banco de dados
- **Auditoria**: Log de todas as acoes importantes no sistema
- **Rate Limiting**: Protecao contra brute-force no login (5 tentativas / 5 min)
- **Table-per-subtype**: Ativos com tipos diferentes usam tabelas de extensao (ativos_veiculo, ativos_epi)

---

## Desenvolvimento Local (sem Docker)

**Pre-requisitos:** JDK 17+, Maven, Node.js 18+, PostgreSQL

### 1. Banco de dados

```bash
# Criar o banco
psql -U postgres -c "CREATE DATABASE systemdemo"
```

### 2. Backend

```bash
cd systemdemo
cd logistica-api
cp ../.env.example .env
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`. O Swagger fica em `http://localhost:8080/docs`.

Na primeira execucao, o Flyway cria as tabelas e popula com os dados de demo.

### 3. Frontend

```bash
cd logistica-app
npm install
npm start
```

A aplicacao abre em `http://localhost:4200`.

---

## Producao (Railway)

### Deploy com Docker Compose

```bash
docker compose up --build
```

O sistema sobe completo:
- PostgreSQL: `localhost:5433`
- API: `localhost:8080`
- Frontend: `localhost:80`

### Variaveis de Ambiente (Railway)

| Variavel | Descricao | Exemplo |
|----------|-----------|---------|
| `JWT_SECRET` | Chave JWT (min 32 chars) | `openssl rand -base64 48` |
| `DB_URL` | URL do PostgreSQL | `jdbc:postgresql://...` |
| `DB_USER` | Usuario do banco | `postgres` |
| `DB_PASSWORD` | Senha do banco | `...` |
| `SPRING_PROFILES_ACTIVE` | Profile do Spring | `production` |
| `EMPRESA_NOME` | Nome da empresa (PDF) | `TechFrame Tecnologia Ltda` |
| `EMPRESA_CNPJ` | CNPJ da empresa (PDF) | `12.345.678/0001-90` |

---

## Seguranca

- **JWT_SECRET** via env var (nao hardcoded)
- **Swagger** desabilitado automaticamente no profile `production`
- **Rate limiting** no login (5 tentativas / 5 min)
- **CORS** restrito ao dominio do frontend
- **BCrypt** para hash de senhas
- **Optimistic Locking** para previnir race conditions

---

## Dados de Demonstracao

O banco e populado automaticamente com:

- **6 funcionarios** da TechFrame (admin, gestor, 4 operacionais)
- **20 ativos**: 4 veiculos, 8 ferramentas, 5 EPIs, 3 outros
- **Historico de ponto** de agosto 2025 (21 dias uteis)
- **Movimentacoes** de ativos (retiradas e devolucoes)
- **Logs de auditoria** de acoes importantes

---

## Demo Publica

Este repositorio e seguro para exposicao publica:

- Todos os dados sao **ficticios** (empresa, CPFs, emails)
- Senhas estao **hasheadas** com BCrypt
- Nao contem dados reais de funcionarios ou clientes
- O `JWT_SECRET` e um valor padrao (trocar em producao)

---

## Licenca

Este e um projeto de demonstracao. O codigo e inspirado em um sistema real de gestao logistica.
