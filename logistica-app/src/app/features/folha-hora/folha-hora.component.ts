import { Component, OnInit, inject } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { PontoService } from '../../core/services/ponto.service';
import { AuthService } from '../../core/services/auth.service';
import { UsuarioService } from '../../core/services/usuario.service';
import { Usuario } from '../../core/models/usuario.model';
import { extrairMensagemErro } from '../../core/utils/erro.util';
import { paraIsoLocal } from '../../core/utils/data.util';

@Component({
  selector: 'app-folha-hora',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
  ],
  templateUrl: './folha-hora.component.html',
  styleUrl: './folha-hora.component.scss',
})
export class FolhaHoraComponent implements OnInit {
  private fb = inject(FormBuilder);
  private pontoService = inject(PontoService);
  private usuarioService = inject(UsuarioService);
  private snackBar = inject(MatSnackBar);
  authService = inject(AuthService);

  gerando = false;
  funcionarios: Usuario[] = [];

  private hoje = new Date();
  private primeiroDiaDoMes = new Date(this.hoje.getFullYear(), this.hoje.getMonth(), 1);

  formulario = this.fb.group({
    inicio: [this.primeiroDiaDoMes, Validators.required],
    fim: [this.hoje, Validators.required],
    // so relevante para ADMIN/GESTOR - deixado vazio, gera a propria folha
    usuarioId: [null as number | null],
  });

  ngOnInit(): void {
    if (this.podeGerarDeOutroUsuario()) {
      this.usuarioService.listar(0, 100).subscribe({
        next: (page) => (this.funcionarios = page.content),
        // se falhar, a tela continua funcionando normalmente para a
        // propria folha do usuario logado - so o seletor fica vazio
        error: () => {},
      });
    }
  }

  podeGerarDeOutroUsuario(): boolean {
    return this.authService.temPapel('ADMIN', 'GESTOR');
  }

  baixar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const valor = this.formulario.getRawValue();
    const inicio = paraIsoLocal(valor.inicio!);
    const fim = paraIsoLocal(valor.fim!);
    const usuarioId = valor.usuarioId ?? undefined;

    this.gerando = true;
    this.pontoService.baixarFolhaHora(inicio, fim, usuarioId).subscribe({
      next: (blob) => {
        this.gerando = false;
        this.disparaDownload(blob, `folha-hora-${inicio}-a-${fim}.pdf`);
      },
      error: (erro: HttpErrorResponse) => {
        this.gerando = false;
        this.snackBar.open(extrairMensagemErro(erro), 'Fechar', { duration: 5000 });
      },
    });
  }

  private disparaDownload(blob: Blob, nomeArquivo: string): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = nomeArquivo;
    link.click();
    window.URL.revokeObjectURL(url);
  }
}
