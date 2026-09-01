import { Component, OnInit, inject, signal } from '@angular/core';
import { NgClass } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { RouterLink } from '@angular/router';
import { UsuarioService } from '../../../core/services/usuario.service';
import { AuthService } from '../../../core/services/auth.service';
import { Usuario } from '../../../core/models/usuario.model';
import { Role } from '../../../core/models/auth.model';

@Component({
  selector: 'app-funcionarios-lista',
  standalone: true,
  imports: [
    NgClass,
    MatTableModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    RouterLink,
  ],
  templateUrl: './lista.component.html',
  styleUrl: './lista.component.scss',
})
export class FuncionariosListaComponent implements OnInit {
  private usuarioService = inject(UsuarioService);
  private snackBar = inject(MatSnackBar);
  authService = inject(AuthService);

  usuarios = signal<Usuario[]>([]);
  carregando = signal(true);
  paginaAtual = signal(0);
  totalPaginas = signal(0);
  totalElementos = signal(0);

  colunas = ['nome', 'email', 'cargo', 'role', 'ativo'];

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.usuarioService.listar(this.paginaAtual()).subscribe({
      next: (resposta) => {
        this.usuarios.set(resposta.content);
        this.totalPaginas.set(resposta.totalPages);
        this.totalElementos.set(resposta.totalElements);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.snackBar.open('Nao foi possivel carregar os funcionarios.', 'Fechar', {
          duration: 4000,
        });
      },
    });
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

  labelRole(role: Role): string {
    const labels: Record<Role, string> = {
      'ADMIN': 'Administrador',
      'GESTOR': 'Gestor',
      'FUNCIONARIO': 'Funcionario',
    };
    return labels[role] ?? role;
  }
}
