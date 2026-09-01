import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { RegistroPonto } from '../models/ponto.model';

@Injectable({ providedIn: 'root' })
export class PontoService {
  private readonly baseUrl = `${environment.apiUrl}/ponto`;

  constructor(private http: HttpClient) {}

  registrarEntrada(): Observable<RegistroPonto> {
    return this.http.post<RegistroPonto>(`${this.baseUrl}/entrada`, {});
  }

  registrarSaidaIntervalo(): Observable<RegistroPonto> {
    return this.http.post<RegistroPonto>(`${this.baseUrl}/intervalo/saida`, {});
  }

  registrarVoltaIntervalo(): Observable<RegistroPonto> {
    return this.http.post<RegistroPonto>(`${this.baseUrl}/intervalo/volta`, {});
  }

  registrarSaida(): Observable<RegistroPonto> {
    return this.http.post<RegistroPonto>(`${this.baseUrl}/saida`, {});
  }

  // sem parametros, o backend assume o mes corrente
  meusRegistros(inicio?: string, fim?: string): Observable<RegistroPonto[]> {
    let params = new HttpParams();
    if (inicio) params = params.set('inicio', inicio);
    if (fim) params = params.set('fim', fim);
    return this.http.get<RegistroPonto[]>(`${this.baseUrl}/meus`, { params });
  }

  registrosDoUsuario(usuarioId: number, inicio: string, fim: string): Observable<RegistroPonto[]> {
    const params = new HttpParams().set('inicio', inicio).set('fim', fim);
    return this.http.get<RegistroPonto[]>(`${this.baseUrl}/usuario/${usuarioId}`, { params });
  }

  baixarFolhaHora(inicio: string, fim: string, usuarioId?: number): Observable<Blob> {
    let params = new HttpParams().set('inicio', inicio).set('fim', fim);
    if (usuarioId) params = params.set('usuarioId', usuarioId.toString());
    return this.http.get(`${this.baseUrl}/folha-hora`, { params, responseType: 'blob' });
  }
}
