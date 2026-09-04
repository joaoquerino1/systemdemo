import { Page, expect } from '@playwright/test';

/** Collects console errors, page errors and failed API requests for later assertion. */
export function coletarErros(page: Page) {
  const erros: string[] = [];

  page.on('console', (msg) => {
    if (msg.type() === 'error') {
      erros.push(`console.error: ${msg.text()}`);
    }
  });

  page.on('pageerror', (err) => {
    erros.push(`pageerror: ${err.message}`);
  });

  page.on('requestfailed', (req) => {
    erros.push(`requestfailed: ${req.method()} ${req.url()} -> ${req.failure()?.errorText}`);
  });

  page.on('response', (res) => {
    if (res.status() >= 400) {
      erros.push(`http ${res.status()}: ${res.request().method()} ${res.url()}`);
    }
  });

  return {
    erros,
    async naoDeveTerErros() {
      // wait for pending network activity to settle before asserting
      await page.waitForLoadState('networkidle').catch(() => {});
      expect(erros, `Erros capturados:\n${erros.join('\n')}`).toEqual([]);
    },
  };
}

export async function fazerLogin(page: Page, email = 'admin@techframe.com', senha = 'demo12345') {
  await page.goto('/login');
  await page.getByLabel('Email').fill(email);
  await page.getByLabel('Senha').fill(senha);
  await page.getByRole('button', { name: /Entrar/ }).click();
  await page.waitForURL('**/dashboard');
}