import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Role } from '../models/auth.model';

export const authGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.estaAutenticado()) {
    router.navigate(['/login']);
    return false;
  }

  // rotas podem declarar quais papeis podem acessar via
  // data: { papeis: ['ADMIN', 'GESTOR'] } no arquivo de rotas
  const papeisPermitidos = route.data?.['papeis'] as Role[] | undefined;
  if (papeisPermitidos && !authService.temPapel(...papeisPermitidos)) {
    router.navigate(['/dashboard']);
    return false;
  }

  return true;
};
