import { Component, inject } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from '../../../core/services/auth.service';
import { Ativo } from '../../../core/models/ativo.model';

export interface ResultadoDialogRetirada {
  usuarioId: number;
  kmSaida?: number;
  observacoes?: string;
}

@Component({
  selector: 'app-dialog-retirada',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  templateUrl: './dialog-retirada.component.html',
})
export class DialogRetiradaComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  dialogRef = inject(MatDialogRef<DialogRetiradaComponent>);
  ativo: Ativo = inject(MAT_DIALOG_DATA);

  // usuarioId vem pre-preenchido com o usuario logado (caso mais comum),
  // mas pode ser trocado - a API permite registrar em nome de outra
  // pessoa por decisao de negocio
  formulario = this.fb.group({
    usuarioId: [this.authService.usuario()?.id ?? null, Validators.required],
    kmSaida: [this.ativo.veiculo?.kmAtual ?? null],
    observacoes: [''],
  });

  confirmar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const valor = this.formulario.getRawValue();
    const resultado: ResultadoDialogRetirada = {
      usuarioId: valor.usuarioId!,
      kmSaida: valor.kmSaida ?? undefined,
      observacoes: valor.observacoes || undefined,
    };
    this.dialogRef.close(resultado);
  }
}
