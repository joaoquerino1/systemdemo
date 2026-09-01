import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AtivoService } from '../../../core/services/ativo.service';
import { StatusAtivo, TipoAtivo } from '../../../core/models/ativo.model';
import { extrairMensagemErro } from '../../../core/utils/erro.util';

@Component({
  selector: 'app-ativo-formulario',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatSnackBarModule,
  ],
  templateUrl: './formulario.component.html',
  styleUrl: './formulario.component.scss',
})
export class AtivoFormularioComponent implements OnInit {
  private fb = inject(FormBuilder);
  private ativoService = inject(AtivoService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  tipos: TipoAtivo[] = ['VEICULO', 'FERRAMENTA', 'EPI', 'OUTRO'];
  statusOpcoes: StatusAtivo[] = ['DISPONIVEL', 'MANUTENCAO', 'INATIVO'];

  // se vier um :codigo na rota, estamos editando; senao, criando
  codigoEmEdicao = signal<string | null>(null);
  salvando = signal(false);

  formulario = this.fb.group({
    tipo: ['VEICULO' as TipoAtivo, Validators.required],
    nome: ['', Validators.required],
    codigo: ['', Validators.required],
    status: ['DISPONIVEL' as StatusAtivo],
    placa: [''],
    marca: [''],
    modelo: [''],
    kmAtual: [0],
    numeroCa: [''],
  });

  ngOnInit(): void {
    const codigo = this.route.snapshot.paramMap.get('codigo');
    if (!codigo) return;

    this.codigoEmEdicao.set(codigo);
    this.formulario.get('codigo')?.disable(); // codigo nao e editavel (ver AtivoService.atualizar no backend)
    this.formulario.get('tipo')?.disable(); // tipo tambem nao muda depois de criado

    this.ativoService.buscarPorCodigo(codigo).subscribe({
      next: (ativo) => {
        this.formulario.patchValue({
          tipo: ativo.tipo,
          nome: ativo.nome,
          codigo: ativo.codigo,
          status: ativo.status,
          placa: ativo.veiculo?.placa ?? '',
          marca: ativo.veiculo?.marca ?? '',
          modelo: ativo.veiculo?.modelo ?? '',
          kmAtual: ativo.veiculo?.kmAtual ?? 0,
          numeroCa: ativo.epi?.numeroCa ?? '',
        });
      },
      error: () => {
        this.snackBar.open('Ativo não encontrado.', 'Fechar', { duration: 4000 });
        this.router.navigate(['/dashboard/ativos']);
      },
    });
  }

  ehVeiculo(): boolean {
    return this.formulario.getRawValue().tipo === 'VEICULO';
  }

  ehEpi(): boolean {
    return this.formulario.getRawValue().tipo === 'EPI';
  }

  salvar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const valor = this.formulario.getRawValue();
    const veiculo = this.ehVeiculo()
      ? { placa: valor.placa!, marca: valor.marca || undefined, modelo: valor.modelo!, kmAtual: valor.kmAtual ?? undefined }
      : undefined;
    const epi = this.ehEpi() ? { numeroCa: valor.numeroCa || undefined } : undefined;

    this.salvando.set(true);

    const codigoEmEdicao = this.codigoEmEdicao();
    const operacao = codigoEmEdicao
      ? this.ativoService.atualizar(codigoEmEdicao, {
          nome: valor.nome!,
          status: valor.status!,
          veiculo,
          epi,
        })
      : this.ativoService.criar({
          tipo: valor.tipo!,
          nome: valor.nome!,
          codigo: valor.codigo!,
          veiculo,
          epi,
        });

    operacao.subscribe({
      next: () => {
        this.salvando.set(false);
        this.snackBar.open('Ativo salvo com sucesso.', 'Fechar', { duration: 3000 });
        this.router.navigate(['/dashboard/ativos']);
      },
      error: (erro: HttpErrorResponse) => {
        this.salvando.set(false);
        this.snackBar.open(extrairMensagemErro(erro), 'Fechar', { duration: 5000 });
      },
    });
  }
}
