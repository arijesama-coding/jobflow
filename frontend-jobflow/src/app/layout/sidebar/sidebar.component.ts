import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  template: `
    <nav class="sidebar">
      <a routerLink="/dashboard" routerLinkActive="active">Dashboard</a>
      <a routerLink="/applications" routerLinkActive="active">Applications</a>
      <a routerLink="/companies" routerLinkActive="active">Companies</a>
      <a routerLink="/jobs" routerLinkActive="active">Job offers</a>
      <a routerLink="/interviews" routerLinkActive="active">Interviews</a>
      <a routerLink="/calendar" routerLinkActive="active">Calendar</a>
      <a routerLink="/follow-ups" routerLinkActive="active">Follow-ups</a>
      <a routerLink="/tasks" routerLinkActive="active">Tasks</a>
      <a routerLink="/documents" routerLinkActive="active">Documents</a>
      <a routerLink="/contacts" routerLinkActive="active">Contacts</a>
      <a routerLink="/analytics" routerLinkActive="active">Analytics</a>
      <a routerLink="/profile" routerLinkActive="active">Profile</a>
    </nav>
  `,
})
export class SidebarComponent {}
