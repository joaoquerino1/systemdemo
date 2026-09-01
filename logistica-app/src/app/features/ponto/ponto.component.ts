import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { HttpErrorResponse } from '@angular/common/http';
import { PontoService } from '../../core/services/ponto.service';
import { RegistroPonto } from '../../core/models/ponto.model';
import { extrairMensagemErro } from '../../core/utils/erro.util';
import { paraIsoLocal } from '../../core/utils/data.util';

@Component({
  selector: 'app-ponto',
  standalone: true,
  imports: [
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
  ],
  templateUrl: './ponto.component.html',
  styleUrl: './ponto.component.scss',
})
export class PontoComponent implements OnInit {
  private pontoService = inject(PontoService);
  private snackBar = inject(MatSnackBar);

  registros = signal<RegistroPonto[]>([]);
  carregando = signal(true);
  registrando = signal(false);

  // o registro de hoje, se existir, dentro da lista do mes corrente
  // (o backend ja retorna o mes corrente por padrao quando nao se
  // informa periodo - ver PontoService.meusRegistros)
  registroDeHoje = computed(() => {
    const hojeIso = paraIsoLocal();
    return this.registros().find((r) => r.data === hojeIso) ?? null;
  });

  colunas = ['data', 'horaEntrada', 'horaSaidaIntervalo', 'horaVoltaIntervalo', 'horaSaida'];

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.pontoService.meusRegistros().subscribe({
      next: (registros) => {
        this.registros.set(registros);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.snackBar.open('Não foi possível carregar seu histórico de ponto.', 'Fechar', {
          duration: 4000,
        });
      },
    });
  }

  bater(acao: 'entrada' | 'saidaIntervalo' | 'voltaIntervalo' | 'saida'): void {
    this.registrando.set(true);

    const chamada = {
      entrada: () => this.pontoService.registrarEntrada(),
      saidaIntervalo: () => this.pontoService.registrarSaidaIntervalo(),
      voltaIntervalo: () => this.pontoService.registrarVoltaIntervalo(),
      saida: () => this.pontoService.registrarSaida(),
    }[acao]();

    chamada.subscribe({
      next: () => {
        this.registrando.set(false);
        this.snackBar.open('Ponto registrado.', 'Fechar', { duration: 3000 });
        this.carregar();
      },
      error: (erro: HttpErrorResponse) => {
        this.registrando.set(false);
        this.snackBar.open(extrairMensagemErro(erro), 'Fechar', { duration: 5000 });
      },
    });
  }

  formatarHora(hora: string | null): string {
    return hora ? hora.substring(0, 5) : '-';
  }
}
