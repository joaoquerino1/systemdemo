import { Component, signal } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterLink, RouterOutlet, MatToolbarModule, MatButtonModule, MatIconModule],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss',
})
export class ShellComponent {
  menuAberto = signal(false);

  constructor(private router: Router, public authService: AuthService) {}

  irParaDashboard(): void {
    this.menuAberto.set(false);
    this.router.navigate(['/dashboard']);
  }

  sair(): void {
    this.menuAberto.set(false);
    this.authService.logout();
  }
}
