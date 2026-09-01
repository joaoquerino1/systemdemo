import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Se o backend responder 401 (token expirado, invalido, ou usuario
 * inativo), desloga automaticamente e manda pro login - evita o
 * usuario ficar "preso" numa tela quebrada com token vencido.
 */
export const erroAutenticacaoInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((erro) => {
      if (erro.status === 401 && !req.url.includes('/auth/login')) {
        authService.logout();
      }
      return throwError(() => erro);
    })
  );
};
