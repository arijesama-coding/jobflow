import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="hero">
      <h1>Manage your job search.<br />Land your next opportunity.</h1>
      <p>Track applications, interviews and follow-ups in one place.</p>
      <a routerLink="/register">Start for free</a>
      <a routerLink="/login">Sign in</a>
    </section>
  `,
})
export class LandingComponent {}
