import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AppComponent } from './app.component';

describe('AppComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      // o AppComponent so contem um <router-outlet>, entao precisa de
      // rotas configuradas (mesmo vazias) para o TestBed conseguir
      // renderizar sem erro
      providers: [provideRouter([])],
    }).compileComponents();
  });

  it('deve criar o componente raiz', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });
});
