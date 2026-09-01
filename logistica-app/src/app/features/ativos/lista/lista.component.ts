import { Component, OnInit, inject, signal } from '@angular/core';
import { NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AtivoService, PaginatedResponse } from '../../../core/services/ativo.service';
import { AuthService } from '../../../core/services/auth.service';
import { Ativo, StatusAtivo, TipoAtivo } from '../../../core/models/ativo.model';
import { extrairMensagemErro } from '../../../core/utils/erro.util';
import {
  DialogRetiradaComponent,
  ResultadoDialogRetirada,
} from '../dialogs/dialog-retirada.component';
import {
  DialogDevolucaoComponent,
  ResultadoDialogDevolucao,
} from '../dialogs/dialog-devolucao.component';

@Component({
  selector: 'app-ativos-lista',
  standalone: true,
  imports: [
    NgClass,
    FormsModule,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDialogModule,
    MatSnackBarModule,
    RouterLink,
  ],
  templateUrl: './lista.component.html',
  styleUrl: './lista.component.scss',
})
export class AtivosListaComponent implements OnInit {
  private ativoService = inject(AtivoService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  authService = inject(AuthService);

  ativos = signal<Ativo[]>([]);
  carregando = signal(true);
  paginaAtual = signal(0);
  totalPaginas = signal(0);
  totalElementos = signal(0);

  // Filtros
  filtroTipo = signal<TipoAtivo | ''>('');
  filtroStatus = signal<StatusAtivo | ''>('');
  filtroBusca = signal('');

  tipos: TipoAtivo[] = ['VEICULO', 'FERRAMENTA', 'EPI', 'OUTRO'];
  statusOpcoes: StatusAtivo[] = ['DISPONIVEL', 'EM_USO', 'MANUTENCAO', 'INATIVO'];

  colunas = ['codigo', 'nome', 'tipo', 'status', 'acoes'];

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    const tipo = this.filtroTipo() || undefined;
    const status = this.filtroStatus() || undefined;

    this.ativoService.listar(tipo, status, this.paginaAtual()).subscribe({
      next: (resposta) => {
        // Filtro de busca por nome/codigo no frontend
        let ativosFiltrados = resposta.content;
        if (this.filtroBusca()) {
          const busca = this.filtroBusca().toLowerCase();
          ativosFiltrados = ativosFiltrados.filter(a =>
            a.nome.toLowerCase().includes(busca) ||
            a.codigo.toLowerCase().includes(busca)
          );
        }
        this.ativos.set(ativosFiltrados);
        this.totalPaginas.set(resposta.totalPages);
        this.totalElementos.set(resposta.totalElements);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.snackBar.open('Nao foi possivel carregar os ativos.', 'Fechar', { duration: 4000 });
      },
    });
  }

  aplicarFiltros(): void {
    this.paginaAtual.set(0);
    this.carregar();
  }

  limparFiltros(): void {
    this.filtroTipo.set('');
    this.filtroStatus.set('');
    this.filtroBusca.set('');
    this.paginaAtual.set(0);
    this.carregar();
  }

  paginaAnterior(): void {
    if (this.paginaAtual() > 0) {
      this.paginaAtual.update(p => p - 1);
      this.carregar();
    }
  }

  proximaPagina(): void {
    if (this.paginaAtual() < this.totalPaginas() - 1) {
      this.paginaAtual.update(p => p + 1);
      this.carregar();
    }
  }

  podeGerenciar(): boolean {
    return this.authService.temPapel('ADMIN', 'GESTOR');
  }

  labelTipo(tipo: TipoAtivo): string {
    const labels: Record<TipoAtivo, string> = {
      'VEICULO': 'Veiculo',
      'FERRAMENTA': 'Ferramenta',
      'EPI': 'EPI',
      'OUTRO': 'Outro',
    };
    return labels[tipo] ?? tipo;
  }

  labelStatus(status: StatusAtivo): string {
    const labels: Record<StatusAtivo, string> = {
      'DISPONIVEL': 'Disponivel',
      'EM_USO': 'Em Uso',
      'MANUTENCAO': 'Em Manutencao',
      'INATIVO': 'Inativo',
    };
    return labels[status] ?? status;
  }

  abrirRetirada(ativo: Ativo): void {
    const dialogRef = this.dialog.open(DialogRetiradaComponent, { data: ativo, width: '400px' });
    dialogRef.afterClosed().subscribe((resultado?: ResultadoDialogRetirada) => {
      if (!resultado) return;

      this.ativoService
        .retirar({ codigoAtivo: ativo.codigo, ...resultado })
        .subscribe({
          next: () => {
            this.snackBar.open('Retirada registrada.', 'Fechar', { duration: 3000 });
            this.carregar();
          },
          error: (erro: HttpErrorResponse) => {
            this.snackBar.open(extrairMensagemErro(erro), 'Fechar', { duration: 5000 });
          },
        });
    });
  }

  abrirDevolucao(ativo: Ativo): void {
    const dialogRef = this.dialog.open(DialogDevolucaoComponent, { data: ativo, width: '400px' });
    dialogRef.afterClosed().subscribe((resultado?: ResultadoDialogDevolucao) => {
      if (!resultado) return;

      this.ativoService
        .devolver({ codigoAtivo: ativo.codigo, ...resultado })
        .subscribe({
          next: () => {
            this.snackBar.open('Devolucao registrada.', 'Fechar', { duration: 3000 });
            this.carregar();
          },
          error: (erro: HttpErrorResponse) => {
            this.snackBar.open(extrairMensagemErro(erro), 'Fechar', { duration: 5000 });
          },
        });
    });
  }

  desativar(ativo: Ativo): void {
    if (!confirm(`Desativar "${ativo.nome}"? Essa acao nao pode ser desfeita pela tela.`)) {
      return;
    }
    this.ativoService.desativar(ativo.codigo).subscribe({
      next: () => {
        this.snackBar.open('Ativo desativado.', 'Fechar', { duration: 3000 });
        this.carregar();
      },
      error: (erro: HttpErrorResponse) => {
        this.snackBar.open(extrairMensagemErro(erro), 'Fechar', { duration: 5000 });
      },
    });
  }

  corDoStatus(status: string): string {
    switch (status) {
      case 'DISPONIVEL': return 'status-disponivel';
      case 'EM_USO': return 'status-em-uso';
      case 'MANUTENCAO': return 'status-manutencao';
      default: return 'status-inativo';
    }
  }
}
