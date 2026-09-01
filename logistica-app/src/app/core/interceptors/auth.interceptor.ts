import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.obterToken();

  // /auth/login nao precisa (e nao deve) de token
  if (!token || req.url.includes('/auth/login')) {
    return next(req);
  }

  const requisicaoComToken = req.clone({
    setHeaders: { Authorization: `Bearer ${token}` },
  });
  return next(requisicaoComToken);
};
