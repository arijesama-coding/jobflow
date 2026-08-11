import { Component, inject } from '@angular/core';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  template: `
    <header class="navbar">
      <span>JobFlow</span>
      <button (click)="authService.logout()">Logout</button>
    </header>
  `,
})
export class NavbarComponent {
  protected readonly authService = inject(AuthService);
}
