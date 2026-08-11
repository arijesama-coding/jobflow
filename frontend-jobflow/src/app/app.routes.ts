import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/role.guard';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./features/landing/landing.component').then(m => m.LandingComponent) },
  { path: 'login', loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent) },
  { path: 'register', loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent) },

  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/shell/shell.component').then(m => m.ShellComponent),
    children: [
      { path: 'dashboard', loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent) },
      { path: 'applications', loadComponent: () => import('./features/applications/applications.component').then(m => m.ApplicationsComponent) },
      { path: 'companies', loadComponent: () => import('./features/companies/companies.component').then(m => m.CompaniesComponent) },
      { path: 'jobs', loadComponent: () => import('./features/jobs/jobs.component').then(m => m.JobsComponent) },
      { path: 'interviews', loadComponent: () => import('./features/interviews/interviews.component').then(m => m.InterviewsComponent) },
      { path: 'follow-ups', loadComponent: () => import('./features/follow-ups/follow-ups.component').then(m => m.FollowUpsComponent) },
      { path: 'tasks', loadComponent: () => import('./features/tasks/tasks.component').then(m => m.TasksComponent) },
      { path: 'documents', loadComponent: () => import('./features/documents/documents.component').then(m => m.DocumentsComponent) },
      { path: 'contacts', loadComponent: () => import('./features/contacts/contacts.component').then(m => m.ContactsComponent) },
      { path: 'analytics', loadComponent: () => import('./features/analytics/analytics.component').then(m => m.AnalyticsComponent) },
      { path: 'profile', loadComponent: () => import('./features/profile/profile.component').then(m => m.ProfileComponent) },
      {
        path: 'admin',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/admin/admin.component').then(m => m.AdminComponent),
      },
    ],
  },

  { path: '**', redirectTo: 'dashboard' },
];
