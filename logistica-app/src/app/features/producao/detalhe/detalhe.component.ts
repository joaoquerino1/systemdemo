import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzModalModule, NzModalService } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzPaginationModule } from 'ng-zorro-antd/pagination';
import { ProducaoService } from '../../../core/services/producao.service';
import { UsuarioService } from '../../../core/services/usuario.service';
import { AuthService } from '../../../core/services/auth.service';
import { Usuario } from '../../../core/models/usuario.model';
import { Etapa, OrdemServico, SerialProducao } from '../../../core/models/producao.model';
import { extrairMensagemErro } from '../../../core/utils/erro.util';
import {
  DialogEtapaComponent,
  DialogEtapaData,
  ModoDialogEtapa,
  ResultadoDialogEtapa,
} from './dialog-etapa.component';

@Component({
  selector: 'app-producao-detalhe',
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
    NzModalModule,
    NzCardModule,
    NzPaginationModule,
  ],
  template: `
    <div class="cabecalho">
      <div>
        <a routerLink="/dashboard/producao" class="voltar">
          <span nz-icon nzType="arrow-left"></span> Ordens de Serviço
        </a>
        @if (os(); as osAtual) {
          <h1>OS {{ osAtual.numero }}</h1>
          <p class="subtitulo">
            {{ osAtual.descricao || 'Sem descrição' }} ·
            <nz-tag [nzColor]="corStatus(osAtual.status)">{{ labelStatus(osAtual.status) }}</nz-tag>
          </p>
        }
      </div>
      @if (podeGerenciar()) {
        <div class="acoes-os">
          <input
            nz-input
            placeholder="Novo serial (ex: SN-2026-001)"
            [ngModel]="novoSerial()"
            (ngModelChange)="novoSerial.set($event)"
            (keyup.enter)="adicionarSerial()"
            [disabled]="serialSalvando()"
            style="width: 240px"
          />
          <button
            nz-button
            nzType="primary"
            (click)="adicionarSerial()"
            [nzLoading]="serialSalvando()"
            [disabled]="!novoSerial().trim()"
          >
            <span nz-icon nzType="plus"></span> Serial
          </button>
        </div>
      }
    </div>

    @if (carregando()) {
      <nz-card><div class="vazio">Carregando cronograma…</div></nz-card>
    } @else {
      @if (os(); as osAtual) {
        @if ((osAtual.seriais?.length ?? 0) === 0) {
          <nz-card><div class="vazio">Nenhum serial nesta OS.</div></nz-card>
        } @else {
          <!-- Navegador e Seletor de Seriais -->
          <nz-card class="card-seletor-seriais">
            <div class="seriais-header">
              <div class="seriais-titulo">
                <span nz-icon nzType="appstore"></span>
                <strong>Seriais da OS ({{ osAtual.seriais?.length }})</strong>
                <span class="seriais-dica">Clique no serial desejado para ver suas etapas:</span>
              </div>
              @if ((osAtual.seriais?.length ?? 0) > 1) {
                <div class="seriais-paginacao-topo">
                  <nz-pagination
                    nzSize="small"
                    [nzPageIndex]="indiceSerialAtual() + 1"
                    [nzPageSize]="1"
                    [nzTotal]="osAtual.seriais?.length ?? 0"
                    (nzPageIndexChange)="aoMudarPaginaSerial($event)"
                  ></nz-pagination>
                </div>
              }
            </div>

            <div class="seriais-botoes-grade">
              @for (serial of osAtual.seriais ?? []; track serial.id; let idx = $index) {
                <button
                  nz-button
                  [nzType]="serialAtivo()?.id === serial.id ? 'primary' : 'default'"
                  (click)="selecionarSerial(serial.id)"
                  class="btn-item-serial"
                  [class.btn-item-serial-ativo]="serialAtivo()?.id === serial.id"
                >
                  <span class="btn-item-idx">{{ idx + 1 }}</span>
                  <span class="btn-item-codigo">{{ serial.codigoSerial }}</span>
                  <nz-tag [nzColor]="corSerial(serial.status)" class="btn-item-tag">
                    {{ labelSerial(serial.status) }}
                  </nz-tag>
                </button>
              }
            </div>
          </nz-card>

          <!-- Detalhe do Serial Selecionado -->
          @if (serialAtivo(); as serial) {
            <nz-card class="card-serial">
              <div class="serial-topo-info">
                <div class="serial-info-principal">
                  <h2>Serial: {{ serial.codigoSerial }}</h2>
                  <nz-tag [nzColor]="corSerial(serial.status)" class="tag-status-serial">
                    {{ labelSerial(serial.status) }}
                  </nz-tag>
                  <span class="posicao-serial">
                    (Serial {{ indiceSerialAtual() + 1 }} de {{ osAtual.seriais?.length }})
                  </span>
                </div>

                @if ((osAtual.seriais?.length ?? 0) > 1) {
                  <div class="navegacao-botoes-serial">
                    <button
                      nz-button
                      nzSize="small"
                      [disabled]="indiceSerialAtual() === 0"
                      (click)="serialAnterior()"
                    >
                      <span nz-icon nzType="left"></span> Anterior
                    </button>
                    <button
                      nz-button
                      nzSize="small"
                      [disabled]="indiceSerialAtual() === (osAtual.seriais?.length ?? 0) - 1"
                      (click)="proximoSerial()"
                    >
                      Próximo <span nz-icon nzType="right"></span>
                    </button>
                  </div>
                }
              </div>

              <nz-table [nzData]="serial.etapas" [nzShowPagination]="false" class="tabela-etapas">
                <thead>
                  <tr>
                    <th>Etapa</th>
                    <th>Colaborador</th>
                    <th>Qtd produzida</th>
                    <th>Início</th>
                    <th>Conclusão</th>
                    <th>Pendências</th>
                    <th nzRight>Ações</th>
                  </tr>
                </thead>
                <tbody>
                  @for (etapa of serial.etapas; track etapa.id) {
                    <tr>
                      <td><strong>{{ etapa.etapaLabel }}</strong></td>
                      <td>{{ etapa.colaboradorNome || '—' }}</td>
                      <td>{{ etapa.quantidadeProduzida ?? '—' }}</td>
                      <td>{{ etapa.dataInicio | date : 'dd/MM/yyyy HH:mm' : 'America/Sao_Paulo' }}</td>
                      <td>{{ etapa.dataConclusao | date : 'dd/MM/yyyy HH:mm' : 'America/Sao_Paulo' }}</td>
                      <td class="celula-pendencias">{{ etapa.pendencias || '—' }}</td>
                      <td nzRight class="acoes">
                        @switch (etapa.status) {
                          @case ('PENDENTE') {
                            <button
                              nz-button
                              nzSize="small"
                              nzType="primary"
                              (click)="abrirAcaoEtapa(etapa, 'INICIAR')"
                            >
                              Iniciar
                            </button>
                          }
                          @case ('EM_ANDAMENTO') {
                            <button
                              nz-button
                              nzSize="small"
                              nzType="primary"
                              (click)="abrirAcaoEtapa(etapa, 'CONCLUIR')"
                            >
                              Concluir
                            </button>
                            <button
                              nz-button
                              nzSize="small"
                              (click)="abrirAcaoEtapa(etapa, 'PENDENCIA')"
                            >
                              Pendência
                            </button>
                          }
                          @case ('CONCLUIDA') {
                            <nz-tag nzColor="green">Concluída</nz-tag>
                            @if (podeGerenciar()) {
                              <button
                                nz-button
                                nzSize="small"
                                nzDanger
                                (click)="abrirAcaoEtapa(etapa, 'REABRIR')"
                              >
                                Reabrir
                              </button>
                            }
                          }
                        }
                      </td>
                    </tr>
                  }
                </tbody>
              </nz-table>
            </nz-card>
          }
        }
      }
    }
  `,
  styles: [
    `
      :host {
        display: block;
      }
      .cabecalho {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        gap: 12px;
        margin-bottom: 16px;
        flex-wrap: wrap;
      }
      h1 {
        font-size: 1.4rem;
        margin: 4px 0;
      }
      .voltar {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        font-size: 0.85rem;
      }
      .subtitulo {
        margin: 0;
        color: rgba(0, 0, 0, 0.45);
      }
      .acoes-os {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
        align-items: center;
      }
      .card-seletor-seriais {
        margin-bottom: 16px;
      }
      .seriais-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 12px;
        flex-wrap: wrap;
        margin-bottom: 12px;
      }
      .seriais-titulo {
        display: flex;
        align-items: center;
        gap: 8px;
        flex-wrap: wrap;
      }
      .seriais-dica {
        color: rgba(0, 0, 0, 0.45);
        font-size: 0.85rem;
      }
      .seriais-botoes-grade {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        align-items: center;
      }
      .btn-item-serial {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        height: auto;
        padding: 6px 12px;
        border-radius: 6px;
        transition: all 0.2s ease;
      }
      .btn-item-idx {
        font-size: 0.8rem;
        opacity: 0.75;
      }
      .btn-item-codigo {
        font-weight: 600;
      }
      .btn-item-tag {
        margin-right: 0;
        font-size: 0.75rem;
        line-height: 18px;
      }
      .card-serial {
        margin-bottom: 16px;
      }
      .serial-topo-info {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 12px;
        flex-wrap: wrap;
        margin-bottom: 16px;
        padding-bottom: 12px;
        border-bottom: 1px solid #f0f0f0;
      }
      .serial-info-principal {
        display: flex;
        align-items: center;
        gap: 10px;
        flex-wrap: wrap;
      }
      .serial-info-principal h2 {
        font-size: 1.2rem;
        margin: 0;
      }
      .posicao-serial {
        color: rgba(0, 0, 0, 0.45);
        font-size: 0.85rem;
      }
      .navegacao-botoes-serial {
        display: flex;
        gap: 8px;
      }
      .celula-pendencias {
        max-width: 220px;
        white-space: normal;
        word-break: break-word;
      }
      .acoes {
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
export class ProducaoDetalheComponent implements OnInit {
  private producaoService = inject(ProducaoService);
  private usuarioService = inject(UsuarioService);
  private route = inject(ActivatedRoute);
  private modal = inject(NzModalService);
  private message = inject(NzMessageService);
  authService = inject(AuthService);

  os = signal<OrdemServico | null>(null);
  carregando = signal(true);
  serialSalvando = signal(false);
  novoSerial = signal('');
  serialSelecionadoId = signal<number | null>(null);

  serialAtivo = computed<SerialProducao | null>(() => {
    const lista = this.os()?.seriais ?? [];
    if (lista.length === 0) return null;
    const selecionado = lista.find((s) => s.id === this.serialSelecionadoId());
    return selecionado ?? lista[0];
  });

  indiceSerialAtual = computed<number>(() => {
    const lista = this.os()?.seriais ?? [];
    const atual = this.serialAtivo();
    if (!atual) return 0;
    const idx = lista.findIndex((s) => s.id === atual.id);
    return idx >= 0 ? idx : 0;
  });

  // Colaboradores so carregados para ADMIN/GESTOR (GET /usuarios e
  // restrito); funcionario inicia sempre para si mesmo.
  colaboradores = signal<Usuario[]>([]);

  podeGerenciar(): boolean {
    return this.authService.temPapel('ADMIN', 'GESTOR');
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.carregarOS(id);
    if (this.podeGerenciar()) {
      this.usuarioService.listar(0, 200).subscribe({
        next: (res) => this.colaboradores.set(res.content.filter((u) => u.ativo)),
        error: () => this.colaboradores.set([]),
      });
    }
  }

  carregarOS(id: number, serialIdParaSelecionar?: number): void {
    this.carregando.set(true);
    this.producaoService.detalharOS(id).subscribe({
      next: (os) => {
        this.os.set(os);
        this.carregando.set(false);

        const seriais = os.seriais ?? [];
        if (seriais.length > 0) {
          if (serialIdParaSelecionar && seriais.some((s) => s.id === serialIdParaSelecionar)) {
            this.serialSelecionadoId.set(serialIdParaSelecionar);
          } else if (
            this.serialSelecionadoId() &&
            seriais.some((s) => s.id === this.serialSelecionadoId())
          ) {
            // Mantem o serial previamente selecionado
          } else {
            this.serialSelecionadoId.set(seriais[0].id);
          }
        } else {
          this.serialSelecionadoId.set(null);
        }
      },
      error: (erro: HttpErrorResponse) => {
        this.carregando.set(false);
        this.message.error(extrairMensagemErro(erro));
      },
    });
  }

  selecionarSerial(id: number): void {
    this.serialSelecionadoId.set(id);
  }

  aoMudarPaginaSerial(pageIndex: number): void {
    const lista = this.os()?.seriais ?? [];
    const novoIdx = pageIndex - 1;
    if (novoIdx >= 0 && novoIdx < lista.length) {
      this.serialSelecionadoId.set(lista[novoIdx].id);
    }
  }

  serialAnterior(): void {
    const lista = this.os()?.seriais ?? [];
    const idx = this.indiceSerialAtual();
    if (idx > 0) {
      this.serialSelecionadoId.set(lista[idx - 1].id);
    }
  }

  proximoSerial(): void {
    const lista = this.os()?.seriais ?? [];
    const idx = this.indiceSerialAtual();
    if (idx < lista.length - 1) {
      this.serialSelecionadoId.set(lista[idx + 1].id);
    }
  }

  adicionarSerial(): void {
    const codigo = this.novoSerial().trim();
    const osAtual = this.os();
    if (!codigo || !osAtual) return;
    this.serialSalvando.set(true);
    this.producaoService.adicionarSerial(osAtual.id, codigo).subscribe({
      next: (novoSerial) => {
        this.serialSalvando.set(false);
        this.novoSerial.set('');
        this.message.success(`Serial ${codigo} adicionado.`);
        this.carregarOS(osAtual.id, novoSerial.id);
      },
      error: (erro: HttpErrorResponse) => {
        this.serialSalvando.set(false);
        this.message.error(extrairMensagemErro(erro));
      },
    });
  }

  abrirAcaoEtapa(etapa: Etapa, modo: ModoDialogEtapa): void {
    const autenticado = this.authService.usuario();
    if (!autenticado) return;

    const data: DialogEtapaData = {
      modo,
      etapa,
      podeEscolherColaborador: this.podeGerenciar() && this.colaboradores().length > 0,
      colaboradores: this.colaboradores(),
      autenticadoId: autenticado.id,
    };

    const modalRef = this.modal.create({
      nzTitle: tituloDialog(modo, etapa),
      nzContent: DialogEtapaComponent,
      nzData: data,
      nzFooter: null,
    });

    modalRef.afterClose.subscribe((res?: ResultadoDialogEtapa) => {
      if (!res) return;
      this.executar(etapa, modo, res);
    });
  }

  private executar(
    etapa: Etapa,
    modo: ModoDialogEtapa,
    res: ResultadoDialogEtapa,
  ): void {
    const aoSucesso = (mensagem: string) => {
      this.message.success(mensagem);
      this.recarregarOS();
    };
    const aoErro = (erro: HttpErrorResponse) =>
      this.message.error(extrairMensagemErro(erro));

    switch (modo) {
      case 'INICIAR':
        this.producaoService
          .iniciarEtapa(etapa.id, res.colaboradorId)
          .subscribe({ next: () => aoSucesso(`Etapa ${etapa.etapaLabel} iniciada.`), error: aoErro });
        break;
      case 'CONCLUIR':
        this.producaoService
          .concluirEtapa(etapa.id, res.quantidadeProduzida ?? 0, res.pendencias)
          .subscribe({ next: () => aoSucesso(`Etapa ${etapa.etapaLabel} concluída.`), error: aoErro });
        break;
      case 'PENDENCIA':
        this.producaoService
          .registrarPendencia(etapa.id, res.pendencias ?? '')
          .subscribe({ next: () => aoSucesso('Pendência registrada.'), error: aoErro });
        break;
      case 'REABRIR':
        this.producaoService
          .reabrirEtapa(etapa.id, res.motivo ?? '')
          .subscribe({ next: () => aoSucesso(`Etapa ${etapa.etapaLabel} reaberta.`), error: aoErro });
        break;
    }
  }

  private recarregarOS(): void {
    const osAtual = this.os();
    if (osAtual) {
      this.carregarOS(osAtual.id, this.serialSelecionadoId() ?? undefined);
    }
  }

  // Labels/cores (mesmas da lista)
  labelStatus(status: OrdemServico['status']): string {
    const labels: Record<string, string> = {
      ABERTA: 'Aberta',
      EM_PRODUCAO: 'Em produção',
      CONCLUIDA: 'Concluída',
      CANCELADA: 'Cancelada',
    };
    return labels[status] ?? status;
  }

  corStatus(status: OrdemServico['status']): string {
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

  labelSerial(status: string): string {
    const labels: Record<string, string> = {
      PENDENTE: 'Pendente',
      EM_PRODUCAO: 'Em produção',
      CONCLUIDO: 'Concluído',
      BLOQUEADO: 'Bloqueado',
    };
    return labels[status] ?? status;
  }

  corSerial(status: string): string {
    switch (status) {
      case 'PENDENTE':
        return 'default';
      case 'EM_PRODUCAO':
        return 'orange';
      case 'CONCLUIDO':
        return 'green';
      default:
        return 'red';
    }
  }
}

function tituloDialog(modo: ModoDialogEtapa, etapa: Etapa): string {
  switch (modo) {
    case 'INICIAR':
      return `Iniciar etapa ${etapa.etapaLabel}`;
    case 'CONCLUIR':
      return `Concluir etapa ${etapa.etapaLabel}`;
    case 'PENDENCIA':
      return `Registrar pendência — ${etapa.etapaLabel}`;
    case 'REABRIR':
      return `Reabrir etapa ${etapa.etapaLabel}`;
  }
}
