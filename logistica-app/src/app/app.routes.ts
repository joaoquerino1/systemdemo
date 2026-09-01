import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { LoginComponent } from './features/auth/login/login.component';
import { LandingComponent } from './features/landing/landing.component';
import { ShellComponent } from './shared/layout/shell.component';
import { HomeComponent } from './features/home.component';
import { AtivosListaComponent } from './features/ativos/lista/lista.component';
import { AtivoFormularioComponent } from './features/ativos/formulario/formulario.component';
import { PontoComponent } from './features/ponto/ponto.component';
import { FolhaHoraComponent } from './features/folha-hora/folha-hora.component';
import { FuncionariosListaComponent } from './features/funcionarios/lista/lista.component';
import { FuncionarioFormularioComponent } from './features/funcionarios/formulario/formulario.component';

export const routes: Routes = [
  { path: '', component: LandingComponent },
  { path: 'login', component: LoginComponent },
  {
    path: 'dashboard',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', component: HomeComponent },
      { path: 'ativos', component: AtivosListaComponent },
      {
        path: 'ativos/novo',
        component: AtivoFormularioComponent,
        data: { papeis: ['ADMIN', 'GESTOR'] },
      },
      {
        path: 'ativos/:codigo/editar',
        component: AtivoFormularioComponent,
        data: { papeis: ['ADMIN', 'GESTOR'] },
      },
      { path: 'ponto', component: PontoComponent },
      { path: 'folha-hora', component: FolhaHoraComponent },
      {
        path: 'funcionarios',
        component: FuncionariosListaComponent,
        data: { papeis: ['ADMIN', 'GESTOR'] },
      },
      {
        path: 'funcionarios/novo',
        component: FuncionarioFormularioComponent,
        data: { papeis: ['ADMIN'] },
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
