import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, map } from 'rxjs';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatButtonModule, MatDividerModule, MatIconModule, MatListModule, MatMenuModule, MatSidenavModule, MatToolbarModule],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss',
})
export class ShellComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly breakpoint = inject(BreakpointObserver);
  readonly mobile = toSignal(this.breakpoint.observe([Breakpoints.Handset]).pipe(map(value => value.matches)), { initialValue: false });
  readonly menuOpen = signal(true);
  readonly pageTitle = signal('Dashboard');
  readonly roleLabel = computed(() => ({ EMPLOYEE: 'Angajat', MANAGER: 'Responsabil departament', ADMIN: 'Administrator' }[this.auth.role() ?? 'EMPLOYEE']));
  readonly nav = computed(() => {
    const items = [
      { label: 'Dashboard', icon: 'space_dashboard', route: '/dashboard', roles: ['EMPLOYEE', 'MANAGER', 'ADMIN'] },
      { label: this.auth.role() === 'EMPLOYEE' ? 'Cererile mele' : 'Toate cererile', icon: 'event_note', route: '/requests', roles: ['EMPLOYEE', 'MANAGER', 'ADMIN'] },
      { label: 'Aprobari', icon: 'task_alt', route: '/approvals', roles: ['MANAGER', 'ADMIN'] },
      { label: 'Calendar echipa', icon: 'calendar_month', route: '/calendar', roles: ['EMPLOYEE', 'MANAGER', 'ADMIN'] },
      { label: 'Rapoarte', icon: 'analytics', route: '/reports', roles: ['MANAGER', 'ADMIN'] },
      { label: 'Administrare', icon: 'settings_suggest', route: '/administration', roles: ['ADMIN'] },
    ];
    return items.filter(item => item.roles.includes(this.auth.role() ?? 'EMPLOYEE'));
  });

  constructor() {
    this.router.events.pipe(filter(event => event instanceof NavigationEnd)).subscribe(() => {
      let route = this.router.routerState.snapshot.root;
      while (route.firstChild) route = route.firstChild;
      this.pageTitle.set(route.data['title'] ?? 'Employee Leave Hub');
      if (this.mobile()) this.menuOpen.set(false);
    });
  }

  ngOnInit(): void { this.auth.refreshUser().subscribe({ error: () => undefined }); }
  initials(name?: string): string { return (name ?? 'U').split(' ').slice(0, 2).map(part => part[0]).join('').toUpperCase(); }
}
