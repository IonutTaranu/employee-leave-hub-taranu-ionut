import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { Role } from './models';

export const authGuard: CanActivateFn = () => inject(AuthService).authenticated() || inject(Router).createUrlTree(['/login']);
export const roleGuard: CanActivateFn = route => {
  const auth = inject(AuthService); const roles = (route.data['roles'] ?? []) as Role[];
  return auth.hasRole(...roles) || inject(Router).createUrlTree(['/dashboard']);
};
