import { Component, inject } from '@angular/core';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { Ativo } from '../../../core/models/ativo.model';

export interface ResultadoDialogDevolucao {
  kmChegada?: number;
  observacoes?: string;
}

@Component({
  selector: 'app-dialog-devolucao',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  templateUrl: './dialog-devolucao.component.html',
})
export class DialogDevolucaoComponent {
  private fb = inject(FormBuilder);
  dialogRef = inject(MatDialogRef<DialogDevolucaoComponent>);
  ativo: Ativo = inject(MAT_DIALOG_DATA);

  formulario = this.fb.group({
    kmChegada: [this.ativo.veiculo?.kmAtual ?? null],
    observacoes: [''],
  });

  confirmar(): void {
    const valor = this.formulario.getRawValue();
    const resultado: ResultadoDialogDevolucao = {
      kmChegada: valor.kmChegada ?? undefined,
      observacoes: valor.observacoes || undefined,
    };
    this.dialogRef.close(resultado);
  }
}
