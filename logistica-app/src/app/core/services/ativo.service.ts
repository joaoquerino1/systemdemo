import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Ativo,
  AtualizarAtivoRequest,
  CriarAtivoRequest,
  DevolucaoAtivoRequest,
  MovimentacaoAtivo,
  RetiradaAtivoRequest,
  StatusAtivo,
  TipoAtivo,
} from '../models/ativo.model';

export interface AtivoStats {
  porStatus: Record<string, number>;
  porTipo: Record<string, number>;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({ providedIn: 'root' })
export class AtivoService {
  private readonly baseUrl = `${environment.apiUrl}/ativos`;
  private readonly movimentacoesUrl = `${environment.apiUrl}/movimentacoes`;

  constructor(private http: HttpClient) {}

  listar(tipo?: TipoAtivo, status?: StatusAtivo, page = 0, size = 20): Observable<PaginatedResponse<Ativo>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (tipo) params = params.set('tipo', tipo);
    if (status) params = params.set('status', status);
    return this.http.get<PaginatedResponse<Ativo>>(this.baseUrl, { params });
  }

  buscarPorCodigo(codigo: string): Observable<Ativo> {
    return this.http.get<Ativo>(`${this.baseUrl}/${codigo}`);
  }

  criar(request: CriarAtivoRequest): Observable<Ativo> {
    return this.http.post<Ativo>(this.baseUrl, request);
  }

  atualizar(codigo: string, request: AtualizarAtivoRequest): Observable<Ativo> {
    return this.http.put<Ativo>(`${this.baseUrl}/${codigo}`, request);
  }

  desativar(codigo: string): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/${codigo}/desativar`, {});
  }

  retirar(request: RetiradaAtivoRequest): Observable<MovimentacaoAtivo> {
    return this.http.post<MovimentacaoAtivo>(`${this.movimentacoesUrl}/retirada`, request);
  }

  devolver(request: DevolucaoAtivoRequest): Observable<MovimentacaoAtivo> {
    return this.http.post<MovimentacaoAtivo>(`${this.movimentacoesUrl}/devolucao`, request);
  }

  obterStats(): Observable<AtivoStats> {
    return this.http.get<AtivoStats>(`${this.baseUrl}/stats`);
  }
}
