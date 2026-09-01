import { Component, signal, inject } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../../core/services/auth.service';
import { extrairMensagemErro } from '../../../core/utils/erro.util';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  carregando = signal(false);
  mensagemErro = signal<string | null>(null);

  formulario = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    senha: ['', [Validators.required]],
  });

  entrar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.carregando.set(true);
    this.mensagemErro.set(null);

    const { email, senha } = this.formulario.getRawValue();
    this.authService.login({ email: email!, senha: senha! }).subscribe({
      next: () => {
        this.carregando.set(false);
        this.router.navigate(['/dashboard']);
      },
      error: (erro: HttpErrorResponse) => {
        this.carregando.set(false);
        this.mensagemErro.set(
          extrairMensagemErro(erro, 'Não foi possível conectar. Tente novamente.')
        );
      },
    });
  }
}
