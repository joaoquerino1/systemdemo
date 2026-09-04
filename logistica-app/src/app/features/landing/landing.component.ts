import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [RouterLink, MatButtonModule, MatIconModule],
  template: `
    <div class="landing">
      <div class="landing-content">
        <div class="demo-badge">DEMO</div>

        <div class="logo-section">
          <mat-icon class="logo-icon">local_shipping</mat-icon>
          <h1>SystemDemo</h1>
          <p class="tagline">Sistema Logistico Interno</p>
          <p class="company">TechFrame Tecnologia Ltda</p>
        </div>

        <div class="features">
          <div class="feature">
            <mat-icon>inventory_2</mat-icon>
            <span>Gestao de Ativos</span>
          </div>
          <div class="feature">
            <mat-icon>access_time</mat-icon>
            <span>Controle de Ponto</span>
          </div>
          <div class="feature">
            <mat-icon>description</mat-icon>
            <span>Folha de Hora</span>
          </div>
          <div class="feature">
            <mat-icon>people</mat-icon>
            <span>Gestao de Funcionarios</span>
          </div>
        </div>

        <div class="demo-info">
          <mat-icon>info</mat-icon>
          <span>Credenciais de demonstracao: <strong>demo&#64;techframe.com</strong> / <strong>demo12345</strong></span>
        </div>

        <a mat-flat-button color="primary" routerLink="/login" class="enter-button">
          <mat-icon>login</mat-icon>
          Entrar no Demo
        </a>
      </div>
    </div>
  `,
  styles: [`
    .landing {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #1976d2 0%, #1565c0 50%, #0d47a1 100%);
      color: white;
      padding: 2rem;
    }

    .landing-content {
      text-align: center;
      max-width: 600px;
    }

    .demo-badge {
      display: inline-block;
      background: rgba(255,255,255,0.2);
      color: white;
      padding: 0.25rem 1rem;
      border-radius: 9999px;
      font-size: 0.75rem;
      font-weight: 700;
      letter-spacing: 0.1em;
      margin-bottom: 2rem;
      border: 1px solid rgba(255,255,255,0.3);
    }

    .logo-section {
      margin-bottom: 2.5rem;

      .logo-icon {
        font-size: 5rem;
        width: 5rem;
        height: 5rem;
        margin-bottom: 1rem;
      }

      h1 {
        font-size: 3rem;
        font-weight: 700;
        margin: 0;
        letter-spacing: -0.02em;
      }

      .tagline {
        font-size: 1.25rem;
        opacity: 0.85;
        margin-top: 0.5rem;
      }

      .company {
        font-size: 0.875rem;
        opacity: 0.6;
        margin-top: 0.25rem;
      }
    }

    .features {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1rem;
      margin-bottom: 2rem;
    }

    .feature {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 1rem 1.25rem;
      background: rgba(255, 255, 255, 0.12);
      border-radius: 12px;
      font-weight: 500;
      backdrop-filter: blur(4px);

      mat-icon {
        color: #bbdefb;
      }
    }

    .demo-info {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
      padding: 0.75rem 1.25rem;
      background: rgba(255, 255, 255, 0.1);
      border-radius: 8px;
      font-size: 0.875rem;
      margin-bottom: 2rem;
      border: 1px solid rgba(255, 255, 255, 0.2);

      strong {
        font-weight: 600;
      }
    }

    .enter-button {
      font-size: 1.125rem;
      padding: 0.75rem 2.5rem;
      border-radius: 12px;
      text-decoration: none;

      mat-icon {
        margin-right: 0.5rem;
      }
    }

    @media (max-width: 500px) {
      .features {
        grid-template-columns: 1fr;
      }

      .logo-section h1 {
        font-size: 2.25rem;
      }
    }
  `],
})
export class LandingComponent {}
