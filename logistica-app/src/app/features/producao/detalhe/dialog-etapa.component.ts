import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NZ_MODAL_DATA, NzModalModule, NzModalRef } from 'ng-zorro-antd/modal';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzInputNumberModule } from 'ng-zorro-antd/input-number';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { Usuario } from '../../../core/models/usuario.model';
import { Etapa } from '../../../core/models/producao.model';

export type ModoDialogEtapa = 'INICIAR' | 'CONCLUIR' | 'PENDENCIA' | 'REABRIR';

export interface DialogEtapaData {
  modo: ModoDialogEtapa;
  etapa: Etapa;
  podeEscolherColaborador: boolean;
  colaboradores: Usuario[];
  autenticadoId: number;
}

/** O que o componente abridor recebe ao fechar o dialog (via resultado()). */
export interface ResultadoDialogEtapa {
  colaboradorId?: number;
  quantidadeProduzida?: number;
  pendencias?: string;
  motivo?: string;
}

/**
 * Modal de acao de etapa. Valida os proprios campos; a chamada HTTP
 * fica com o componente abridor (mesmo padrao dos dialogs de ativos).
 */
@Component({
  selector: 'app-dialog-etapa',
  standalone: true,
  imports: [
    FormsModule,
    NzButtonModule,
    NzFormModule,
    NzInputModule,
    NzInputNumberModule,
    NzSelectModule,
  ],
  template: `
    <p class="hint">{{ textoAjuda() }}</p>
    @if (erro()) {
      <p class="erro">{{ erro() }}</p>
    }

    @switch (data.modo) {
      @case ('INICIAR') {
        @if (data.podeEscolherColaborador) {
          <nz-form-item>
            <nz-form-label [nzSpan]="8">Colaborador</nz-form-label>
            <nz-form-control [nzSpan]="16">
              <nz-select [(ngModel)]="colaboradorId" nzAllowClear nzPlaceHolder="Selecione…">
                @for (colaborador of data.colaboradores; track colaborador.id) {
                  <nz-option [nzValue]="colaborador.id" [nzLabel]="colaborador.nome"></nz-option>
                }
              </nz-select>
            </nz-form-control>
          </nz-form-item>
        }
      }
      @case ('CONCLUIR') {
        <nz-form-item>
          <nz-form-label [nzSpan]="8" nzRequired>Quantidade</nz-form-label>
          <nz-form-control [nzSpan]="16">
            <nz-input-number
              [(ngModel)]="quantidade"
              [nzMin]="1"
              [nzPrecision]="0"
              style="width: 100%"
              nzPlaceHolder="Ex: 120"
            ></nz-input-number>
          </nz-form-control>
        </nz-form-item>
        <nz-form-item>
          <nz-form-label [nzSpan]="8">Pendências</nz-form-label>
          <nz-form-control [nzSpan]="16">
            <textarea
              nz-input
              rows="3"
              [(ngModel)]="pendencias"
              placeholder="Observações finais da etapa (opcional)"
            ></textarea>
          </nz-form-control>
        </nz-form-item>
      }
      @case ('PENDENCIA') {
        <nz-form-item>
          <nz-form-control>
            <textarea
              nz-input
              rows="4"
              [(ngModel)]="pendencias"
              nz-required
              placeholder="Descreva a pendência…"
            ></textarea>
          </nz-form-control>
        </nz-form-item>
      }
      @case ('REABRIR') {
        <nz-form-item>
          <nz-form-control>
            <textarea
              nz-input
              rows="3"
              [(ngModel)]="motivo"
              nz-required
              placeholder="Motivo da reabertura (vai para a auditoria)"
            ></textarea>
          </nz-form-control>
        </nz-form-item>
      }
    }

    <div class="rodape">
      <button nz-button (click)="fechar()">Cancelar</button>
      <button nz-button nzType="primary" (click)="confirmar()">Confirmar</button>
    </div>
  `,
  styles: [
    `
      .hint {
        margin-top: 0;
        color: rgba(0, 0, 0, 0.45);
      }
      .rodape {
        display: flex;
        justify-content: flex-end;
        gap: 8px;
        margin-top: 16px;
      }
      .erro {
        color: #ff4d4f;
        margin: 0 0 4px;
      }
    `,
  ],
})
export class DialogEtapaComponent {
  readonly data = inject(NZ_MODAL_DATA) as DialogEtapaData;
  private modalRef = inject(NzModalRef);

  colaboradorId: number | null = null;
  quantidade: number | null = null;
  pendencias = '';
  motivo = '';
  erro = signal('');

  // Mensagem por modo quando os campos obrigatorios nao estao ok.
  private static readonly MENSAGENS_VALIDACAO: Record<ModoDialogEtapa, string> = {
    INICIAR: '',
    CONCLUIR: 'Informe a quantidade produzida (maior que zero).',
    PENDENCIA: 'O texto da pendência é obrigatório.',
    REABRIR: 'O motivo da reabertura é obrigatório.',
  };

  // Valor inicial do select de colaborador: quem ja esta na etapa ou o
  // usuario autenticado (o backend usa o autenticado quando o campo
  // nao vem na requisicao).
  constructor() {
    this.colaboradorId = this.data.etapa.colaboradorId ?? this.data.autenticadoId;
    this.pendencias = this.data.etapa.pendencias ?? '';
  }

  /** Chamado pelo botao Confirmar dentro do modal (nzFooter: null). */
  confirmar(): void {
    if (!this.valido()) {
      this.erro.set(DialogEtapaComponent.MENSAGENS_VALIDACAO[this.modo]);
      return;
    }
    this.modalRef.close(this.resultado());
  }

  fechar(): void {
    this.modalRef.destroy();
  }

  get modo(): ModoDialogEtapa {
    return this.data.modo;
  }

  textoAjuda(): string {
    switch (this.data.modo) {
      case 'INICIAR':
        return this.data.podeEscolherColaborador
          ? 'Selecione o colaborador responsável (vazio = você).'
          : 'A etapa será registrada em seu nome.';
      case 'CONCLUIR':
        return 'Quantidade produzida é obrigatória (maior que zero).';
      case 'PENDENCIA':
        return 'A pendência fica registrada sem concluir a etapa.';
      case 'REABRIR':
        return 'A etapa volta para EM_ANDAMENTO e o motivo é auditado.';
    }
  }

  /** Chamado pelo abridor para checar os campos antes de agir. */
  valido(): boolean {
    switch (this.modo) {
      case 'INICIAR':
        return true;
      case 'CONCLUIR':
        return this.quantidade !== null && this.quantidade > 0;
      case 'PENDENCIA':
        return this.pendencias.trim().length > 0;
      case 'REABRIR':
        return this.motivo.trim().length > 0;
    }
  }

  resultado(): ResultadoDialogEtapa {
    switch (this.modo) {
      case 'INICIAR':
        return { colaboradorId: this.colaboradorId ?? undefined };
      case 'CONCLUIR':
        return {
          quantidadeProduzida: this.quantidade ?? undefined,
          pendencias: this.pendencias.trim() || undefined,
        };
      case 'PENDENCIA':
        return { pendencias: this.pendencias.trim() };
      case 'REABRIR':
        return { motivo: this.motivo.trim() };
    }
  }
}
