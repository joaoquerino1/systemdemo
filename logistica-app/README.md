# logistica-app

Frontend Angular (PWA) do sistema logístico interno — consome a API
`logistica-api`.

## Stack

- Angular 18 (standalone components, signals)
- Angular Material
- PWA (`@angular/pwa`) — instalável em desktop e mobile
- Reactive Forms

## Pré-requisitos

- Node.js 18+ e npm

## Rodando localmente

```bash
npm install
npm start
```

Abre em `http://localhost:4200`. Por padrão aponta para a API em
`http://localhost:8080/api` (`src/environments/environment.ts`) — ajuste
se sua API estiver rodando em outra porta/endereço.

**Importante:** a API precisa estar rodando (ver `logistica-api/README.md`)
e já ter pelo menos um usuário criado via `/api/usuarios/bootstrap-admin`
para conseguir logar.

## Estrutura

```bash
src/app/
├── core/
│ ├── services/ # AuthService, AtivoService, PontoService, etc.
│ ├── interceptors/ # anexa o JWT, trata 401
│ ├── guards/ # authGuard (protege rotas, checa papel)
│ └── models/ # interfaces TypeScript espelhando os DTOs da API
├── features/
│ ├── auth/login/ # tela de login
│ ├── ativos/ # cadastro, listagem, retirada/devolução de ativos
│ ├── ponto/ # bater ponto, histórico
│ ├── folha-hora/ # geração de PDF da folha de hora
│ └── funcionarios/ # cadastro e listagem de funcionários (ADMIN/GESTOR)
└── shared/
└── layout/ # shell.component (barra superior + logout)
´´´

## Estado atual

- Login funcional, com token JWT persistido (sobrevive a F5)
- Guard de rota (`authGuard`), com suporte a restrição por papel via
  `data: { papeis: ['ADMIN', 'GESTOR'] }` nas rotas
- Interceptor que anexa o token automaticamente e desloga em caso de 401
- Telas de Ativos, Ponto, Folha de Hora e Funcionários implementadas

## Build de produção

```bash
npm run build
```

Gera os arquivos estáticos em `dist/logistica-app/browser`. Em produção, o
deploy é feito no **Railway** a partir do `Dockerfile` deste diretório (build
multi-stage: Node compila os arquivos estáticos, depois um Nginx enxuto os
serve). O `Dockerfile` já ajusta `apiUrl` automaticamente no build via a
`ARG API_URL` — configurada no Railway.
