import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { NzTableModule, NzTableQueryParams } from 'ng-zorro-antd/table';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzMessageService } from 'ng-zorro-antd/message';
import { ProducaoService } from '../../../core/services/producao.service';
import { AuthService } from '../../../core/services/auth.service';
import {
  OrdemServico,
  StatusOS,
} from '../../../core/models/producao.model';
import { extrairMensagemErro } from '../../../core/utils/erro.util';

@Component({
  selector: 'app-producao-lista',
  standalone: true,
  imports: [
    DatePipe,
    FormsModule,
    RouterLink,
    NzTableModule,
    NzTagModule,
    NzButtonModule,
    NzIconModule,
    NzInputModule,
    NzSelectModule,
    NzCardModule,
  ],
  template: `
    <div class="cabecalho">
      <h1>Produção — Ordens de Serviço</h1>
      @if (podeGerenciar()) {
        <button nz-button nzType="primary" routerLink="/dashboard/producao/nova">
          <span nz-icon nzType="plus"></span>
          Nova OS
        </button>
      }
    </div>

    <nz-card class="filtros">
      <div class="filtros-linha">
        <nz-input-group [nzPrefix]="prefixBusca" nzAllowClear class="filtro-busca">
          <input
            type="text"
            nz-input
            placeholder="Buscar por nome ou número da OS..."
            [ngModel]="filtroBusca()"
            (ngModelChange)="aoMudarBusca($event)"
            (keyup.enter)="aplicarFiltros()"
          />
        </nz-input-group>
        <ng-template #prefixBusca>
          <span nz-icon nzType="search"></span>
        </ng-template>

        <nz-select
          class="filtro-status"
          [ngModel]="filtroStatus()"
          (ngModelChange)="filtroStatus.set($event); aplicarFiltros()"
          nzAllowClear
          nzPlaceHolder="Status"
        >
          @for (op of statusOpcoes; track op) {
            <nz-option [nzValue]="op" [nzLabel]="labelStatus(op)"></nz-option>
          }
        </nz-select>

        <button nz-button nzType="primary" (click)="aplicarFiltros()">
          <span nz-icon nzType="search"></span>
          Buscar
        </button>

        @if (filtroBusca() || filtroStatus()) {
          <button nz-button nzType="default" (click)="limparFiltros()">
            Limpar
          </button>
        }
      </div>
    </nz-card>

    <nz-table
      #t
      [nzData]="ordens()"
      [nzFrontPagination]="false"
      [nzLoading]="carregando()"
      [nzTotal]="totalElementos()"
      [nzPageIndex]="paginaAtual() + 1"
      [nzPageSize]="tamanhoPagina()"
      (nzQueryParams)="aoMudarPagina($event)"
    >
      <thead>
        <tr>
          <th>OS</th>
          <th>Descrição</th>
          <th>Progresso</th>
          <th>Status</th>
          <th>Criada em</th>
          <th nzRight>Ações</th>
        </tr>
      </thead>
      <tbody>
        @for (os of ordens(); track os.id) {
          <tr>
            <td><strong>{{ os.numero }}</strong></td>
            <td>{{ os.descricao || '—' }}</td>
            <td>
              @if (os.totalSeriais !== null && os.totalSeriais > 0) {
                {{ os.seriaisConcluidos }}/{{ os.totalSeriais }} seriais concluídos
              } @else {
                —
              }
            </td>
            <td>
              <nz-tag [nzColor]="corStatus(os.status)">{{ labelStatus(os.status) }}</nz-tag>
            </td>
            <td>{{ os.criadoEm | date : 'dd/MM/yyyy' }}</td>
            <td nzRight class="acoes">
              <a routerLink="/dashboard/producao/{{ os.id }}">Abrir cronograma</a>
            </td>
          </tr>
        } @empty {
          <tr><td colspan="6" class="vazio">Nenhuma OS encontrada.</td></tr>
        }
      </tbody>
    </nz-table>
  `,
  styles: [
    `
      :host {
        display: block;
      }
      .cabecalho {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;
        gap: 12px;
        flex-wrap: wrap;
      }
      h1 {
        font-size: 1.4rem;
        margin: 0;
      }
      .filtros {
        margin-bottom: 16px;
      }
      .filtros-linha {
        display: flex;
        gap: 12px;
        flex-wrap: wrap;
        align-items: center;
      }
      .filtro-busca {
        max-width: 320px;
        min-width: 220px;
        flex: 1;
      }
      .filtro-status {
        min-width: 180px;
      }
      .acoes a {
        white-space: nowrap;
      }
      .vazio {
        text-align: center;
        color: rgba(0, 0, 0, 0.45);
        padding: 24px 0;
      }
    `,
  ],
})
export class ProducaoListaComponent implements OnInit {
  private producaoService = inject(ProducaoService);
  private message = inject(NzMessageService);
  authService = inject(AuthService);

  ordens = signal<OrdemServico[]>([]);
  carregando = signal(true);
  paginaAtual = signal(0);
  tamanhoPagina = signal(20);
  totalElementos = signal(0);
  filtroBusca = signal('');
  filtroStatus = signal<StatusOS | ''>('');

  statusOpcoes: StatusOS[] = ['ABERTA', 'EM_PRODUCAO', 'CONCLUIDA', 'CANCELADA'];

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    const status = this.filtroStatus() || undefined;
    const busca = this.filtroBusca().trim() || undefined;
    this.producaoService
      .listarOS(status, busca, this.paginaAtual(), this.tamanhoPagina())
      .subscribe({
        next: (resposta) => {
          this.ordens.set(resposta.content);
          this.totalElementos.set(resposta.totalElements);
          this.carregando.set(false);
        },
        error: (erro: HttpErrorResponse) => {
          this.carregando.set(false);
          this.message.error(extrairMensagemErro(erro));
        },
      });
  }

  aoMudarBusca(valor: string): void {
    this.filtroBusca.set(valor);
    if (!valor || !valor.trim()) {
      this.aplicarFiltros();
    }
  }

  limparFiltros(): void {
    this.filtroBusca.set('');
    this.filtroStatus.set('');
    this.aplicarFiltros();
  }

  aoMudarPagina(params: NzTableQueryParams): void {
    this.paginaAtual.set(params.pageIndex - 1);
    this.tamanhoPagina.set(params.pageSize);
    this.carregar();
  }

  aplicarFiltros(): void {
    this.paginaAtual.set(0);
    this.carregar();
  }

  podeGerenciar(): boolean {
    return this.authService.temPapel('ADMIN', 'GESTOR');
  }

  labelStatus(status: StatusOS): string {
    const labels: Record<StatusOS, string> = {
      ABERTA: 'Aberta',
      EM_PRODUCAO: 'Em produção',
      CONCLUIDA: 'Concluída',
      CANCELADA: 'Cancelada',
    };
    return labels[status] ?? status;
  }

  corStatus(status: StatusOS): string {
    switch (status) {
      case 'ABERTA':
        return 'blue';
      case 'EM_PRODUCAO':
        return 'orange';
      case 'CONCLUIDA':
        return 'green';
      default:
        return 'default';
    }
  }
}
