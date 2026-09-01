import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, Role, UsuarioAutenticado } from '../models/auth.model';

const CHAVE_TOKEN = 'logistica_token';
const CHAVE_USUARIO = 'logistica_usuario';

@Injectable({ providedIn: 'root' })
export class AuthService {
  // signal com o usuario atual - null quando deslogado. Inicializa lendo
  // o que ja estiver salvo no localStorage, para sobreviver a um F5.
  private usuarioSignal = signal<UsuarioAutenticado | null>(this.carregarUsuarioSalvo());

  usuario = computed(() => this.usuarioSignal());
  estaAutenticado = computed(() => this.usuarioSignal() !== null);

  constructor(private http: HttpClient, private router: Router) {}

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, request).pipe(
      tap((resposta) => {
        localStorage.setItem(CHAVE_TOKEN, resposta.token);
        const usuario: UsuarioAutenticado = {
          id: resposta.id,
          nome: resposta.nome,
          email: resposta.email,
          role: resposta.role,
        };
        localStorage.setItem(CHAVE_USUARIO, JSON.stringify(usuario));
        this.usuarioSignal.set(usuario);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(CHAVE_TOKEN);
    localStorage.removeItem(CHAVE_USUARIO);
    this.usuarioSignal.set(null);
    this.router.navigate(['/login']);
  }

  obterToken(): string | null {
    return localStorage.getItem(CHAVE_TOKEN);
  }

  temPapel(...papeis: Role[]): boolean {
    const usuario = this.usuarioSignal();
    return usuario !== null && papeis.includes(usuario.role);
  }

  private carregarUsuarioSalvo(): UsuarioAutenticado | null {
    const bruto = localStorage.getItem(CHAVE_USUARIO);
    return bruto ? JSON.parse(bruto) : null;
  }
}
