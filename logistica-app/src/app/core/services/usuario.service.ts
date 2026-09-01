import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CriarUsuarioRequest, Usuario } from '../models/usuario.model';

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
}

export interface UsuarioStats {
  totalUsuarios: number;
  usuariosAtivos: number;
}

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly baseUrl = `${environment.apiUrl}/usuarios`;

  constructor(private http: HttpClient) {}

  listar(page = 0, size = 20): Observable<PageResponse<Usuario>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PageResponse<Usuario>>(this.baseUrl, { params });
  }

  criar(request: CriarUsuarioRequest): Observable<Usuario> {
    return this.http.post<Usuario>(this.baseUrl, request);
  }

  obterStats(): Observable<UsuarioStats> {
    return this.http.get<UsuarioStats>(`${this.baseUrl}/stats`);
  }
}
