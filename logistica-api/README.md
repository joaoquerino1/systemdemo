# logistica-api

Backend do sistema logístico interno (ativos/inventário, ponto e folha de hora)
para empresa de esquadrias de alumínio e vidro.

## Stack

- Java 17 + Spring Boot 3.3.2
- PostgreSQL + Flyway (migrations em `src/main/resources/db/migration`)
- Spring Security + JWT
- OpenPDF (geração da folha de hora)
- Testcontainers + MockMvc (testes end-to-end)

## Pré-requisitos

- JDK 17+
- Maven
- PostgreSQL instalado localmente (nativo, ou gerenciado via pgAdmin)
- Docker (opcional — só necessário para rodar os testes de integração, que sobem
  um PostgreSQL isolado via Testcontainers)

## Configurando o banco local

1. Crie o banco de dados (via pgAdmin ou `psql`):
```sql
   CREATE DATABASE logistica_db;
```
2. Garanta que o usuário `postgres` tem a senha `postgres` (ou ajuste as
   variáveis de ambiente abaixo para bater com sua configuração):
```sql
   ALTER USER postgres WITH PASSWORD 'postgres';
```

Os defaults do `application.yml` já apontam para
`jdbc:postgresql://localhost:5432/logistica_db` com usuário/senha `postgres`/`postgres`
e schema `producao` (criado automaticamente pelo Flyway na primeira execução).

Se sua configuração for diferente, exporte antes de rodar:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/logistica_db
export DB_USER=postgres
export DB_PASSWORD=sua_senha
export DB_SCHEMA=producao
```

## Rodando localmente

```bash
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`, com Swagger em `http://localhost:8080/docs`.

### Criar o primeiro usuário (admin)

**Linux/macOS:**
```bash
curl -X POST http://localhost:8080/api/usuarios/bootstrap-admin \
  -H "Content-Type: application/json" \
  -d '{"nome":"Admin","cpf":"11111111111","matricula":"0001","email":"admin@admin.com","senha":"senha12345"}'
```

**Windows (PowerShell):**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/usuarios/bootstrap-admin" `
  -Method Post `
  -ContentType "application/json" `
  -Body '{"nome":"Admin","cpf":"11111111111","matricula":"0001","email":"admin@admin.com","senha":"senha12345"}'
```

Esse endpoint só funciona **uma vez** (enquanto não existir nenhum usuário cadastrado).
Depois disso, use `POST /api/auth/login` para pegar o token JWT e `POST /api/usuarios`
(autenticado como ADMIN) para criar os demais usuários.

## Rodando os testes

```bash
mvn test
```

Os testes usam Testcontainers, que sobe um PostgreSQL isolado automaticamente
via Docker (precisa do Docker rodando na máquina só para este passo — não
interfere no banco local usado por `spring-boot:run`). O arquivo principal é
`src/test/java/com/logistica/sistema/e2e/FluxoCompletoE2ETest.java`, com 27
cenários cobrindo autenticação, CRUD de ativos, movimentação, ponto e folha de hora.

## Estrutura

src/main/java/com/logistica/sistema/
├── controller/ # endpoints REST
├── service/ # regras de negócio
├── repository/ # Spring Data JPA
├── model/ # entidades JPA
├── dto/ # request/response
├── security/ # JWT, autenticação
└── exception/ # tratamento de erros


## Produção

Deploy feito no **Railway**, a partir do `Dockerfile` deste diretório (build
multi-stage: Maven compila o jar, depois roda em uma imagem `eclipse-temurin:17-jre`
enxuta). O Railway injeta as variáveis de ambiente de produção automaticamente
(incluindo `PORT`).

## Variáveis de ambiente (produção)

| Variável | Descrição | Padrão (dev) |
|---|---|---|
| `DB_URL` | URL JDBC completa do Postgres | `jdbc:postgresql://localhost:5432/logistica_db` |
| `DB_USER` / `DB_PASSWORD` | credenciais do Postgres | `postgres` / `postgres` |
| `DB_SCHEMA` | schema do banco (produção/homologação) | `producao` |
| `PORT` | porta em que o servidor sobe — definida automaticamente pelo Railway | `8080` |
| `JWT_SECRET` | chave de assinatura do JWT — **trocar em produção** | valor de exemplo, inseguro |
| `EMPRESA_NOME` / `EMPRESA_CNPJ` | usados no cabeçalho da folha de hora em PDF | valores de exemplo |
| `CORS_ORIGENS` | origens liberadas para o frontend (separadas por vírgula) | `http://localhost:4200` |

## Pendências conhecidas

- Módulo de EPI/Ferramentas: modelo de dados pronto (`AtivoEpi`), mas sem regras de
  negócio específicas ainda (ex: alerta de validade do CA)
- Cálculo automático de horas extras/atrasos/faltas na folha de hora depende de uma
  jornada padrão da empresa ainda não definida — hoje sai em branco para
  preenchimento manual