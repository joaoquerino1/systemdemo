import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CriarOrdemServicoRequest,
  Etapa,
  OrdemServico,
  PageResponse,
  SerialProducao,
  StatusOS,
} from '../models/producao.model';

@Injectable({ providedIn: 'root' })
export class ProducaoService {
  private readonly baseUrl = `${environment.apiUrl}/producao`;

  constructor(private http: HttpClient) {}

  criarOS(request: CriarOrdemServicoRequest): Observable<OrdemServico> {
    return this.http.post<OrdemServico>(`${this.baseUrl}/ordens`, request);
  }

  listarOS(
    status?: StatusOS,
    busca?: string,
    page = 0,
    size = 20,
  ): Observable<PageResponse<OrdemServico>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (status) params = params.set('status', status);
    if (busca && busca.trim()) params = params.set('busca', busca.trim());
    return this.http.get<PageResponse<OrdemServico>>(`${this.baseUrl}/ordens`, { params });
  }

  detalharOS(id: number): Observable<OrdemServico> {
    return this.http.get<OrdemServico>(`${this.baseUrl}/ordens/${id}`);
  }

  adicionarSerial(osId: number, codigoSerial: string): Observable<SerialProducao> {
    return this.http.post<SerialProducao>(`${this.baseUrl}/ordens/${osId}/seriais`, {
      codigoSerial,
    });
  }

  iniciarEtapa(etapaId: number, colaboradorId?: number): Observable<Etapa> {
    return this.http.post<Etapa>(`${this.baseUrl}/etapas/${etapaId}/iniciar`, {
      colaboradorId: colaboradorId ?? null,
    });
  }

  concluirEtapa(
    etapaId: number,
    quantidadeProduzida: number,
    pendencias?: string,
  ): Observable<Etapa> {
    return this.http.post<Etapa>(`${this.baseUrl}/etapas/${etapaId}/concluir`, {
      quantidadeProduzida,
      pendencias: pendencias ?? null,
    });
  }

  registrarPendencia(etapaId: number, texto: string): Observable<Etapa> {
    return this.http.put<Etapa>(`${this.baseUrl}/etapas/${etapaId}/pendencias`, { texto });
  }

  reabrirEtapa(etapaId: number, motivo: string): Observable<Etapa> {
    return this.http.post<Etapa>(`${this.baseUrl}/etapas/${etapaId}/reabrir`, { motivo });
  }
}
