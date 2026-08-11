import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { NavbarComponent } from '../navbar/navbar.component';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, SidebarComponent, NavbarComponent],
  template: `
    <div class="shell">
      <app-sidebar />
      <div class="shell-main">
        <app-navbar />
        <main class="shell-content">
          <router-outlet />
        </main>
      </div>
    </div>
  `,
})
export class ShellComponent {}
