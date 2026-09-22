import { ApplicationConfig, provideZoneChangeDetection, isDevMode, LOCALE_ID } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { registerLocaleData } from '@angular/common';
import pt from '@angular/common/locales/pt';

import { routes } from './app.routes';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideServiceWorker } from '@angular/service-worker';
import { provideNativeDateAdapter } from '@angular/material/core';
import { provideNzI18n, pt_BR } from 'ng-zorro-antd/i18n';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { erroAutenticacaoInterceptor } from './core/interceptors/erro-autenticacao.interceptor';

registerLocaleData(pt);

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideAnimationsAsync(),
    // ng-zorro (telas de Producao) - locale pt-BR para datas/messages
    provideNzI18n(pt_BR),
    { provide: LOCALE_ID, useValue: 'pt' },
    provideNativeDateAdapter(),
    provideHttpClient(withInterceptors([authInterceptor, erroAutenticacaoInterceptor])),
    provideServiceWorker('ngsw-worker.js', {
      enabled: !isDevMode(),
      registrationStrategy: 'registerWhenStable:30000',
    }),
  ],
};
