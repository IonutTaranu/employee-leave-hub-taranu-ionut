import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './core/guards';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./features/login/login.component').then(m => m.LoginComponent) },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/shell.component').then(m => m.ShellComponent),
    children: [
      { path: 'dashboard', title: 'Dashboard', data: { title: 'Dashboard' }, loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent) },
      { path: 'requests', title: 'Cereri de concediu', data: { title: 'Cereri de concediu' }, loadComponent: () => import('./features/requests/requests.component').then(m => m.RequestsComponent) },
      { path: 'approvals', title: 'Aprobari', canActivate: [roleGuard], data: { title: 'Aprobari', approvals: true, roles: ['MANAGER', 'ADMIN'] }, loadComponent: () => import('./features/requests/requests.component').then(m => m.RequestsComponent) },
      { path: 'calendar', title: 'Calendar echipa', data: { title: 'Calendar echipa' }, loadComponent: () => import('./features/calendar/calendar.component').then(m => m.CalendarComponent) },
      { path: 'reports', title: 'Rapoarte', canActivate: [roleGuard], data: { title: 'Rapoarte', roles: ['MANAGER', 'ADMIN'] }, loadComponent: () => import('./features/reports/reports.component').then(m => m.ReportsComponent) },
      { path: 'administration', title: 'Administrare', canActivate: [roleGuard], data: { title: 'Administrare', roles: ['ADMIN'] }, loadComponent: () => import('./features/administration/administration.component').then(m => m.AdministrationComponent) },
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
    ],
  },
  { path: '**', redirectTo: '' },
];
