import { Component, inject, signal } from '@angular/core';
import { FormsModule, ReactiveFormsModule, NonNullableFormBuilder, FormArray, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzMessageService } from 'ng-zorro-antd/message';
import { ProducaoService } from '../../../core/services/producao.service';
import { extrairMensagemErro } from '../../../core/utils/erro.util';

@Component({
  selector: 'app-producao-formulario',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    NzFormModule,
    NzInputModule,
    NzButtonModule,
    NzIconModule,
    NzCardModule,
  ],
  template: `
    <div class="cabecalho">
      <h1>Nova Ordem de Serviço</h1>
    </div>

    <nz-card>
      <form nz-form [formGroup]="form" (ngSubmit)="salvar()">
        <nz-form-item>
          <nz-form-label [nzSpan]="6" nzRequired>Numero da OS</nz-form-label>
          <nz-form-control [nzSpan]="14" nzErrorTip="Informe o numero da OS">
            <input nz-input formControlName="numero" placeholder="Ex: OS-2026-017" />
          </nz-form-control>
        </nz-form-item>

        <nz-form-item>
          <nz-form-label [nzSpan]="6">Descrição</nz-form-label>
          <nz-form-control [nzSpan]="14">
            <input nz-input formControlName="descricao" placeholder="Descrição do pedido/projeto (opcional)" />
          </nz-form-control>
        </nz-form-item>

        <nz-form-item>
          <nz-form-label [nzSpan]="6" nzRequired nzFor="seriais">Seriais</nz-form-label>
          <nz-form-control [nzSpan]="14" nzErrorTip="Cadastre ao menos um serial">
            <div formArrayName="seriais" class="seriais-lista">
              @for (controle of seriais.controls; track $index) {
                <div class="serial-linha" [formGroupName]="$index">
                  <input
                    nz-input
                    formControlName="codigo"
                    placeholder="Ex: SN-2026-001"
                    [id]="$index === 0 ? 'seriais' : 'seriais-' + $index"
                  />
                  <button
                    nz-button
                    nzType="dashed"
                    type="button"
                    (click)="removerSerial($index)"
                    [disabled]="seriais.length === 1"
                    title="Remover serial"
                  >
                    <span nz-icon nzType="minus"></span>
                  </button>
                </div>
              }
              <button nz-button nzType="dashed" type="button" (click)="adicionarSerial()" block>
                <span nz-icon nzType="plus"></span> Adicionar serial
              </button>
            </div>
          </nz-form-control>
        </nz-form-item>

        <nz-form-item>
          <nz-form-control [nzSpan]="14" [nzOffset]="6">
            <div class="acoes-form">
              <button nz-button nzType="primary" type="submit" [nzLoading]="salvando()">
                Criar OS
              </button>
              <button nz-button type="button" (click)="voltar()">Cancelar</button>
            </div>
          </nz-form-control>
        </nz-form-item>
      </form>
    </nz-card>
  `,
  styles: [
    `
      :host {
        display: block;
        max-width: 760px;
      }
      .cabecalho {
        margin-bottom: 16px;
      }
      h1 {
        font-size: 1.4rem;
        margin: 0;
      }
      .seriais-lista {
        display: flex;
        flex-direction: column;
        gap: 8px;
      }
      .serial-linha {
        display: flex;
        gap: 8px;
      }
      .serial-linha input {
        flex: 1;
      }
      .acoes-form {
        display: flex;
        gap: 8px;
      }
    `,
  ],
})
export class ProducaoFormularioComponent {
  private producaoService = inject(ProducaoService);
  private message = inject(NzMessageService);
  private router = inject(Router);
  private fb = inject(NonNullableFormBuilder);

  salvando = signal(false);

  form = this.fb.group({
    numero: ['', [Validators.required]],
    descricao: [''],
    seriais: this.fb.array([this.criarGrupoSerial()]),
  });

  get seriais(): FormArray {
    return this.form.controls.seriais;
  }

  criarGrupoSerial() {
    return this.fb.group({
      codigo: ['', [Validators.required]],
    });
  }

  adicionarSerial(): void {
    this.seriais.push(this.criarGrupoSerial());
  }

  removerSerial(indice: number): void {
    if (this.seriais.length > 1) {
      this.seriais.removeAt(indice);
    }
  }

  salvar(): void {
    if (this.form.invalid) {
      // marca todos como tocados para exibir os erros
      Object.values(this.form.controls).forEach((c) => c.markAsDirty());
      this.seriais.controls.forEach((grupo) =>
        Object.values((grupo as any).controls).forEach((c: any) => c.markAsDirty()),
      );
      return;
    }

    const codigos = this.seriais.controls
      .map((c) => c.getRawValue().codigo.trim())
      .filter((c) => c.length > 0);

    this.salvando.set(true);
    this.producaoService
      .criarOS({
        numero: this.form.getRawValue().numero.trim(),
        descricao: this.form.getRawValue().descricao || undefined,
        seriais: codigos,
      })
      .subscribe({
        next: (os) => {
          this.salvando.set(false);
          this.message.success(`OS ${os.numero} criada com ${codigos.length} serial(is).`);
          this.router.navigate(['/dashboard/producao', os.id]);
        },
        error: (erro: HttpErrorResponse) => {
          this.salvando.set(false);
          this.message.error(extrairMensagemErro(erro));
        },
      });
  }

  voltar(): void {
    this.router.navigate(['/dashboard/producao']);
  }
}
