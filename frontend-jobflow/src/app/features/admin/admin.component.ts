import { Component } from '@angular/core';

@Component({
  selector: 'app-admin',
  standalone: true,
  template: `
    <div class="page">
      <h1>Admin</h1>
      <p>Admin dashboard — users, audit logs, global stats (Phase 12).</p>
    </div>
  `,
})
export class AdminComponent {}
