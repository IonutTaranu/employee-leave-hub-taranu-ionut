import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { AuthResponse, Role, UserSummary } from './models';

const TOKEN_KEY = 'leavehub_token';
const USER_KEY = 'leavehub_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly userState = signal<UserSummary | null>(this.readUser());
  readonly user = this.userState.asReadonly();
  readonly authenticated = computed(() => !!this.userState() && !!this.token);
  readonly role = computed<Role | null>(() => this.userState()?.role ?? null);

  get token(): string | null { return localStorage.getItem(TOKEN_KEY); }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/login', { email, password }).pipe(tap(response => {
      localStorage.setItem(TOKEN_KEY, response.token);
      localStorage.setItem(USER_KEY, JSON.stringify(response.user));
      this.userState.set(response.user);
    }));
  }

  refreshUser(): Observable<UserSummary> {
    return this.http.get<UserSummary>('/api/auth/me').pipe(tap(user => {
      localStorage.setItem(USER_KEY, JSON.stringify(user));
      this.userState.set(user);
    }));
  }

  hasRole(...roles: Role[]): boolean { const role = this.role(); return role !== null && roles.includes(role); }
  logout(redirect = true): void {
    localStorage.removeItem(TOKEN_KEY); localStorage.removeItem(USER_KEY); this.userState.set(null);
    if (redirect) void this.router.navigate(['/login']);
  }

  private readUser(): UserSummary | null {
    try { const value = localStorage.getItem(USER_KEY); return value ? JSON.parse(value) as UserSummary : null; }
    catch { return null; }
  }
}
