import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData } from 'chart.js';
import { AuthService } from '../core/services/auth.service';
import { AtivoService } from '../core/services/ativo.service';
import { PontoService } from '../core/services/ponto.service';
import { UsuarioService } from '../core/services/usuario.service';
import { RegistroPonto } from '../core/models/ponto.model';

interface DashboardStats {
  ativos: {
    porStatus: Record<string, number>;
    porTipo: Record<string, number>;
  };
  usuarios: {
    total: number;
    ativos: number;
  };
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, MatCardModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule, BaseChartDirective],
  template: `
    <div class="dashboard fade-in">
      <div class="welcome-section">
        <h1>Bem-vindo, {{ authService.usuario()?.nome }}</h1>
        <p>{{ mensagemSaudacao() }}</p>
      </div>

      @if (carregando()) {
        <div class="carregando">
          <mat-spinner diameter="32"></mat-spinner>
        </div>
      } @else {
        <!-- Stats Grid -->
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon" style="background: #e3f2fd; color: #1976d2;">
              <mat-icon>inventory_2</mat-icon>
            </div>
            <span class="stat-label">Total de Ativos</span>
            <span class="stat-value">{{ totalAtivos() }}</span>
          </div>

          <div class="stat-card">
            <div class="stat-icon" style="background: #dcfce7; color: #16a34a;">
              <mat-icon>check_circle</mat-icon>
            </div>
            <span class="stat-label">Disponiveis</span>
            <span class="stat-value">{{ ativosPorStatus().get('DISPONIVEL') ?? 0 }}</span>
          </div>

          <div class="stat-card">
            <div class="stat-icon" style="background: #fef3c7; color: #d97706;">
              <mat-icon>pending</mat-icon>
            </div>
            <span class="stat-label">Em Uso</span>
            <span class="stat-value">{{ ativosPorStatus().get('EM_USO') ?? 0 }}</span>
          </div>

          <div class="stat-card">
            <div class="stat-icon" style="background: #fee2e2; color: #dc2626;">
              <mat-icon>build</mat-icon>
            </div>
            <span class="stat-label">Em Manutencao</span>
            <span class="stat-value">{{ ativosPorStatus().get('MANUTENCAO') ?? 0 }}</span>
          </div>

          @if (authService.temPapel('ADMIN', 'GESTOR')) {
            <div class="stat-card">
              <div class="stat-icon" style="background: #ede9fe; color: #7c3aed;">
                <mat-icon>people</mat-icon>
              </div>
              <span class="stat-label">Funcionarios</span>
              <span class="stat-value">{{ totalUsuarios() }}</span>
            </div>
          }
        </div>

        <!-- Charts Section -->
        <div class="charts-grid">
          <mat-card class="chart-card">
            <mat-card-content>
              <h3>Status dos Ativos</h3>
              <div class="chart-container">
                <canvas baseChart
                  [data]="statusChartData"
                  [type]="'doughnut'"
                  [options]="doughnutOptions">
                </canvas>
              </div>
            </mat-card-content>
          </mat-card>

          <mat-card class="chart-card">
            <mat-card-content>
              <h3>Ativos por Tipo</h3>
              <div class="chart-container">
                <canvas baseChart
                  [data]="tipoChartData"
                  [type]="'bar'"
                  [options]="barOptions">
                </canvas>
              </div>
            </mat-card-content>
          </mat-card>
        </div>

        <!-- Time Tracking Chart -->
        <mat-card class="chart-card full-width">
          <mat-card-content>
            <h3>Horas Trabalhadas - Agosto 2025</h3>
            <p class="chart-subtitle">Horas diarias dos funcionarios no mes atual</p>
            <div class="chart-container wide">
              <canvas baseChart
                [data]="horasChartData"
                [type]="'line'"
                [options]="lineOptions">
              </canvas>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Quick Actions -->
        <h2>Acoes Rapidas</h2>
        <div class="quick-actions">
          <a routerLink="/dashboard/ponto" class="action-card">
            <mat-icon>access_time</mat-icon>
            <span>Bater Ponto</span>
          </a>
          <a routerLink="/dashboard/ativos" class="action-card">
            <mat-icon>inventory_2</mat-icon>
            <span>Ver Ativos</span>
          </a>
          <a routerLink="/dashboard/folha-hora" class="action-card">
            <mat-icon>description</mat-icon>
            <span>Folha de Hora</span>
          </a>
          @if (authService.temPapel('ADMIN', 'GESTOR')) {
            <a routerLink="/dashboard/funcionarios" class="action-card">
              <mat-icon>people</mat-icon>
              <span>Funcionarios</span>
            </a>
          }
        </div>

        <!-- Today's Clock Status -->
        @if (pontoDeHoje()) {
          <mat-card class="card status-card">
            <mat-card-content>
              <div class="status-header">
                <mat-icon>schedule</mat-icon>
                <span>Seu Ponto de Hoje</span>
              </div>
              <div class="status-times">
                <div class="time-item">
                  <span class="time-label">Entrada</span>
                  <span class="time-value">{{ formatarHora(pontoDeHoje()?.horaEntrada) }}</span>
                </div>
                <div class="time-item">
                  <span class="time-label">Saida Intervalo</span>
                  <span class="time-value">{{ formatarHora(pontoDeHoje()?.horaSaidaIntervalo) }}</span>
                </div>
                <div class="time-item">
                  <span class="time-label">Volta Intervalo</span>
                  <span class="time-value">{{ formatarHora(pontoDeHoje()?.horaVoltaIntervalo) }}</span>
                </div>
                <div class="time-item">
                  <span class="time-label">Saida</span>
                  <span class="time-value">{{ formatarHora(pontoDeHoje()?.horaSaida) }}</span>
                </div>
              </div>
              <a routerLink="/dashboard/ponto" mat-stroked-button color="primary" class="mt-3">
                Acessar Ponto
              </a>
            </mat-card-content>
          </mat-card>
        }
      }
    </div>
  `,
  styles: [`
    .dashboard {
      max-width: 1100px;
    }

    .welcome-section {
      margin-bottom: 2rem;

      h1 {
        margin-bottom: 0.25rem;
      }

      p {
        color: #64748b;
        font-size: 1rem;
      }
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
      gap: 1rem;
      margin-bottom: 2rem;
    }

    .stat-card {
      background: white;
      border-radius: 12px;
      border: 1px solid #e2e8f0;
      padding: 1.25rem;
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      transition: all 0.2s ease;

      &:hover {
        border-color: #1976d2;
        box-shadow: 0 4px 12px rgba(25, 118, 210, 0.1);
      }

      .stat-icon {
        width: 40px;
        height: 40px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      .stat-label {
        font-size: 0.75rem;
        font-weight: 600;
        text-transform: uppercase;
        letter-spacing: 0.05em;
        color: #64748b;
      }

      .stat-value {
        font-size: 2rem;
        font-weight: 700;
        color: #1e293b;
        line-height: 1;
      }
    }

    /* Charts */
    .charts-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1.5rem;
      margin-bottom: 2rem;
    }

    .chart-card {
      h3 {
        font-size: 1rem;
        font-weight: 600;
        margin: 0 0 0.5rem 0;
        color: #1e293b;
      }

      .chart-subtitle {
        font-size: 0.75rem;
        color: #64748b;
        margin: 0 0 1rem 0;
      }
    }

    .chart-card.full-width {
      margin-bottom: 2rem;
    }

    .chart-container {
      position: relative;
      height: 250px;
      display: flex;
      align-items: center;
      justify-content: center;

      &.wide {
        height: 300px;
      }
    }

    /* Quick Actions */
    .quick-actions {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
      gap: 1rem;
      margin-bottom: 2rem;
    }

    .action-card {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 0.75rem;
      padding: 1.5rem 1rem;
      background: white;
      border-radius: 12px;
      border: 1px solid #e2e8f0;
      text-decoration: none;
      color: #1e293b;
      font-weight: 500;
      transition: all 0.2s ease;

      mat-icon {
        font-size: 2rem;
        width: 2rem;
        height: 2rem;
        color: #1976d2;
      }

      &:hover {
        border-color: #1976d2;
        background: #e3f2fd;
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(25, 118, 210, 0.15);
      }
    }

    .status-card {
      max-width: 500px;
    }

    .status-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 1rem;
      font-weight: 600;
      color: #1e293b;

      mat-icon {
        color: #1976d2;
      }
    }

    .status-times {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 0.75rem;
    }

    .time-item {
      display: flex;
      flex-direction: column;
      gap: 2px;

      .time-label {
        font-size: 0.75rem;
        color: #64748b;
      }

      .time-value {
        font-size: 1.125rem;
        font-weight: 600;
        color: #1e293b;
      }
    }

    .mt-3 {
      margin-top: 1rem;
    }

    @media (max-width: 768px) {
      .charts-grid {
        grid-template-columns: 1fr;
      }
    }

    @media (max-width: 600px) {
      .stats-grid {
        grid-template-columns: 1fr 1fr;
      }

      .quick-actions {
        grid-template-columns: 1fr 1fr;
      }
    }
  `],
})
export class HomeComponent implements OnInit {
  authService = inject(AuthService);
  private ativoService = inject(AtivoService);
  private pontoService = inject(PontoService);
  private usuarioService = inject(UsuarioService);

  carregando = signal(true);
  ativosPorStatus = signal<Map<string, number>>(new Map());
  totalAtivos = signal(0);
  totalUsuarios = signal(0);
  pontoDeHoje = signal<RegistroPonto | null>(null);

  // Chart data
  statusChartData: ChartData<'doughnut'> = { labels: [], datasets: [] };
  tipoChartData: ChartData<'bar'> = { labels: [], datasets: [] };
  horasChartData: ChartData<'line'> = { labels: [], datasets: [] };

  doughnutOptions: ChartConfiguration<'doughnut'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { position: 'bottom', labels: { padding: 16, font: { size: 12 } } }
    }
  };

  barOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    indexAxis: 'y',
    plugins: { legend: { display: false } },
    scales: {
      x: { grid: { display: false } },
      y: { grid: { display: false } }
    }
  };

  lineOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { position: 'top', labels: { padding: 16, font: { size: 11 } } }
    },
    scales: {
      y: {
        min: 0,
        max: 10,
        ticks: { stepSize: 2, callback: (v) => v + 'h' }
      },
      x: { grid: { display: false } }
    }
  };

  ngOnInit(): void {
    this.carregarDados();
  }

  mensagemSaudacao(): string {
    const hora = new Date().getHours();
    if (hora < 12) return 'Bom dia! Aqui esta o resumo do sistema demo.';
    if (hora < 18) return 'Boa tarde! Aqui esta o resumo do sistema demo.';
    return 'Boa noite! Aqui esta o resumo do sistema demo.';
  }

  formatarHora(hora: string | null | undefined): string {
    return hora ? hora.substring(0, 5) : '-';
  }

  private carregarDados(): void {
    this.ativoService.obterStats().subscribe({
      next: (stats) => {
        const porStatus = new Map<string, number>();
        let total = 0;
        for (const [key, value] of Object.entries(stats.porStatus ?? {})) {
          porStatus.set(key, value as number);
          total += value as number;
        }
        this.ativosPorStatus.set(porStatus);
        this.totalAtivos.set(total);

        // Status doughnut chart
        this.statusChartData = {
          labels: ['Disponivel', 'Em Uso', 'Manutencao', 'Inativo'],
          datasets: [{
            data: [
              porStatus.get('DISPONIVEL') ?? 0,
              porStatus.get('EM_USO') ?? 0,
              porStatus.get('MANUTENCAO') ?? 0,
              porStatus.get('INATIVO') ?? 0
            ],
            backgroundColor: ['#22c55e', '#f59e0b', '#ef4444', '#94a3b8'],
            borderWidth: 0
          }]
        };

        // Type bar chart
        const tipoLabels: string[] = [];
        const tipoData: number[] = [];
        const tipoColors: string[] = ['#1976d2', '#f59e0b', '#22c55e', '#7c3aed'];
        for (const [key, value] of Object.entries(stats.porTipo ?? {})) {
          const label = key === 'VEICULO' ? 'Veiculos' :
                       key === 'FERRAMENTA' ? 'Ferramentas' :
                       key === 'EPI' ? 'EPIs' : 'Outros';
          tipoLabels.push(label);
          tipoData.push(value as number);
        }
        this.tipoChartData = {
          labels: tipoLabels,
          datasets: [{
            data: tipoData,
            backgroundColor: tipoColors.slice(0, tipoData.length),
            borderRadius: 6
          }]
        };
      },
      error: () => {},
    });

    if (this.authService.temPapel('ADMIN', 'GESTOR')) {
      this.usuarioService.obterStats().subscribe({
        next: (stats) => {
          this.totalUsuarios.set(stats.totalUsuarios ?? 0);
        },
        error: () => {},
      });
    }

    this.pontoService.meusRegistros().subscribe({
      next: (registros) => {
        const hoje = new Date().toISOString().split('T')[0];
        const pontoHoje = registros.find(r => r.data === hoje);
        this.pontoDeHoje.set(pontoHoje ?? null);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
      },
    });

    // Time tracking line chart - demo data
    this.horasChartData = {
      labels: ['01', '04', '05', '06', '07', '08', '11', '12', '13', '14', '15', '18', '19', '20', '21', '22', '25', '26', '27', '28', '29'],
      datasets: [
        {
          label: 'Joao Pedro',
          data: [8.08, 8.17, 8.08, 8.33, 8.08, 8.08, 8.17, 8.08, 8.42, 8.08, 7.5, 8.17, 8.08, 8.08, 8.42, 8.08, 8.17, 8.08, 8.08, 8.08, 8.08],
          borderColor: '#1976d2',
          backgroundColor: 'rgba(25, 118, 210, 0.1)',
          tension: 0.3,
          fill: false,
          pointRadius: 3
        },
        {
          label: 'Maria Fernanda',
          data: [8.0, 8.0, 8.08, 8.0, 8.0, 8.0, 8.0, 8.08, 8.0, 8.0, 8.0, 8.0, 8.0, 8.0, 8.08, 8.0, 8.0, 8.0, 8.0, 8.0, 8.08],
          borderColor: '#f59e0b',
          backgroundColor: 'rgba(245, 158, 11, 0.1)',
          tension: 0.3,
          fill: false,
          pointRadius: 3
        },
        {
          label: 'Rafael Costa',
          data: [8.0, 8.0, 8.0, 8.5, 8.0, 8.0, 8.0, 8.0, 8.0, 8.0, 8.0, 8.0, 8.0, 8.0, 8.0, 8.0, 8.0, 8.0, 8.0, 8.0, 8.0],
          borderColor: '#22c55e',
          backgroundColor: 'rgba(34, 197, 94, 0.1)',
          tension: 0.3,
          fill: false,
          pointRadius: 3
        }
      ]
    };
  }
}
