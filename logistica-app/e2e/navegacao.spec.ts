import { test, expect } from '@playwright/test';
import { coletarErros, fazerLogin } from './helpers';

test.describe('Navegacao apos login (ADMIN)', () => {
  test.beforeEach(async ({ page }) => {
    await fazerLogin(page);
  });

  test('dashboard carrega estatisticas e graficos', async ({ page }) => {
    const erros = coletarErros(page);

    await expect(page.getByRole('heading', { name: /Bem-vindo, Carlos Mendes/ })).toBeVisible();
    await expect(page.getByText('Total de Ativos')).toBeVisible();
    await expect(page.getByText('Em Manutencao')).toBeVisible();
    await expect(page.getByText('Status dos Ativos')).toBeVisible();
    await expect(page.getByText('Ativos por Tipo')).toBeVisible();
    await expect(page.getByText(/Horas Trabalhadas/)).toBeVisible();
    // stat values rendered (20 seed assets)
    await expect(page.locator('.stat-value').first()).toHaveText('20');

    await erros.naoDeveTerErros();
  });

  test('lista de ativos mostra dados do seed', async ({ page }) => {
    const erros = coletarErros(page);

    await page.goto('/dashboard/ativos');
    await expect(page.getByRole('heading', { name: 'Ativos' })).toBeVisible();
    await expect(page.getByText('Fiorino Branca - TF')).toBeVisible();
    await expect(page.getByText('Broca SDS-Plus 10mm')).toBeVisible();

    await erros.naoDeveTerErros();
  });

  test('formulario de novo ativo abre', async ({ page }) => {
    const erros = coletarErros(page);

    await page.goto('/dashboard/ativos/novo');
    await expect(page.getByRole('heading', { name: 'Novo ativo' })).toBeVisible();

    await erros.naoDeveTerErros();
  });

  test('pagina de ponto carrega', async ({ page }) => {
    const erros = coletarErros(page);

    await page.goto('/dashboard/ponto');
    await expect(page.getByRole('heading', { name: 'Meu Ponto' })).toBeVisible();

    await erros.naoDeveTerErros();
  });

  test('folha de hora carrega', async ({ page }) => {
    const erros = coletarErros(page);

    await page.goto('/dashboard/folha-hora');
    await expect(page.getByRole('heading', { name: 'Folha de Hora' })).toBeVisible();

    await erros.naoDeveTerErros();
  });

  test('lista de funcionarios mostra dados do seed', async ({ page }) => {
    const erros = coletarErros(page);

    await page.goto('/dashboard/funcionarios');
    await expect(page.getByRole('heading', { name: 'Funcionários' })).toBeVisible();
    await expect(page.getByRole('cell', { name: 'Carlos Mendes' })).toBeVisible();
    await expect(page.getByRole('cell', { name: 'Ana Beatriz Silva' })).toBeVisible();

    await erros.naoDeveTerErros();
  });

  test('navegacao pelo menu lateral funciona', async ({ page }) => {
    const erros = coletarErros(page);

    await page.getByRole('link', { name: /Ativos/ }).first().click();
    await expect(page).toHaveURL(/\/dashboard\/ativos/);

    await page.getByRole('link', { name: /Ponto/ }).first().click();
    await expect(page).toHaveURL(/\/dashboard\/ponto/);

    await page.getByRole('link', { name: /Folha de Hora/ }).first().click();
    await expect(page).toHaveURL(/\/dashboard\/folha-hora/);

    await page.getByRole('link', { name: /Funcionários/ }).first().click();
    await expect(page).toHaveURL(/\/dashboard\/funcionarios/);

    await erros.naoDeveTerErros();
  });
});