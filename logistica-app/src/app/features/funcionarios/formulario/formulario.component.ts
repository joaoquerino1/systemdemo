import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { UsuarioService } from '../../../core/services/usuario.service';
import { Role } from '../../../core/models/auth.model';
import { extrairMensagemErro } from '../../../core/utils/erro.util';

@Component({
  selector: 'app-funcionario-formulario',
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
export class FuncionarioFormularioComponent {
  private fb = inject(FormBuilder);
  private usuarioService = inject(UsuarioService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  salvando = signal(false);
  papeis: Role[] = ['FUNCIONARIO', 'GESTOR', 'ADMIN'];

  formulario = this.fb.group({
    nome: ['', Validators.required],
    cpf: ['', [Validators.required, Validators.pattern(/^\d{11}$/)]],
    matricula: ['', Validators.required],
    cargo: [''],
    setor: [''],
    email: ['', [Validators.required, Validators.email]],
    senha: ['', [Validators.required, Validators.minLength(8)]],
    role: ['FUNCIONARIO' as Role, Validators.required],
  });

  salvar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.salvando.set(true);
    const valor = this.formulario.getRawValue();

    this.usuarioService
      .criar({
        nome: valor.nome!,
        cpf: valor.cpf!,
        matricula: valor.matricula!,
        cargo: valor.cargo || undefined,
        setor: valor.setor || undefined,
        email: valor.email!,
        senha: valor.senha!,
        role: valor.role!,
      })
      .subscribe({
        next: () => {
          this.salvando.set(false);
          this.snackBar.open('Funcionário criado com sucesso.', 'Fechar', { duration: 3000 });
          this.router.navigate(['/dashboard/funcionarios']);
        },
        error: (erro: HttpErrorResponse) => {
          this.salvando.set(false);
          this.snackBar.open(extrairMensagemErro(erro), 'Fechar', { duration: 5000 });
        },
      });
  }
}
