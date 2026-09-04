# E2E tests (Playwright)

End-to-end tests that exercise the real UI against a running stack.

## Against the local Docker stack

```bash
docker compose up --build -d   # from the repo root (systemdemo/)
npm run test:e2e               # from logistica-app/
```

Runs against `http://localhost` (default in `playwright.config.ts`).

## Against a live deployment

```bash
E2E_BASE_URL=https://your-app.up.railway.app npm run test:e2e
```

## What it covers

- `login.spec.ts` — landing page, valid login, invalid login (and that the
  backend rejects bad credentials)
- `navegacao.spec.ts` — dashboard (charts render without console errors),
  ativos, novo ativo, ponto, folha-hora, funcionários, and sidebar navigation

Every spec captures browser console errors, page errors, and failed/4xx/5xx
requests and fails the test if any occur.

Login credentials used by the tests come from the demo seed data
(`V5__seed_demo_data.sql`): `admin@techframe.com` / `demo12345`.

`test-results/` (artifacts) is gitignored.

> First run downloads the Chromium browser (`npx playwright install chromium`).
