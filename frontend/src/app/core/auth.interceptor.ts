import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const token = auth.token;
  const apiRequest = request.url.startsWith('/api') ? request.clone({
    url: `http://localhost:8080${request.url}`,
    setHeaders: token ? { Authorization: `Bearer ${token}` } : {},
  }) : request;
  return next(apiRequest).pipe(catchError((error: HttpErrorResponse) => {
    if (error.status === 401 && !request.url.endsWith('/auth/login')) auth.logout();
    return throwError(() => error);
  }));
};
