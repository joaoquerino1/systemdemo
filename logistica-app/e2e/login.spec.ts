import { test, expect } from '@playwright/test';
import { coletarErros, fazerLogin } from './helpers';

test.describe('Autenticacao', () => {
  test('landing page carrega e links para o login', async ({ page }) => {
    const erros = coletarErros(page);

    await page.goto('/');
    await expect(page.getByRole('heading', { name: 'SystemDemo' })).toBeVisible();
    await expect(page.getByText('Sistema Logistico Interno')).toBeVisible();

    await page.getByRole('link', { name: /Entrar no Demo/i }).click();
    await expect(page).toHaveURL(/\/login/);
    await expect(page.getByRole('heading', { name: 'Bem-vindo' })).toBeVisible();

    await erros.naoDeveTerErros();
  });

  test('login valido redireciona para o dashboard', async ({ page }) => {
    const erros = coletarErros(page);
    await fazerLogin(page);
    await expect(page).toHaveURL(/\/dashboard/);
    await erros.naoDeveTerErros();
  });

  test('login invalido mostra mensagem de erro', async ({ page }) => {
    await page.goto('/login');
    await page.getByLabel('Email').fill('admin@techframe.com');
    await page.getByLabel('Senha').fill('senha-errada');
    await page.getByRole('button', { name: /Entrar/ }).click();

    await expect(page.getByText(/Credenciais invalidas|email ou senha/i)).toBeVisible();
    await expect(page).toHaveURL(/\/login/);
    // 401 is the expected behavior here, so no strict zero-error assertion
  });
});