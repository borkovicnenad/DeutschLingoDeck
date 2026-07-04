import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Observable, catchError, switchMap, throwError } from 'rxjs';

import { AuthService } from '../auth/auth.service';
import { TokenStorageService } from '../auth/token-storage.service';

const AUTH_ENDPOINTS = ['/auth/register', '/auth/login', '/auth/refresh', '/auth/logout'];

/**
 * Handles expired access tokens (HTTP 401) by requesting a new access token
 * and retrying the original request once.
 *
 * TODO: concurrent requests failing at the same time will each trigger their
 * own refresh call. A shared in-flight refresh Observable should be
 * introduced before production to guarantee refresh-token rotation is only
 * triggered once per expiry.
 */
export const refreshTokenInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const tokenStorage = inject(TokenStorageService);

  const isAuthEndpoint = AUTH_ENDPOINTS.some((endpoint) => req.url.includes(endpoint));

  return next(req).pipe(
    catchError((error: unknown) => {
      const isUnauthorized = error instanceof HttpErrorResponse && error.status === 401;

      if (!isUnauthorized || isAuthEndpoint) {
        return throwError(() => error);
      }

      return authService.refreshAccessToken().pipe(
        switchMap((accessToken) =>
          next(
            req.clone({
              setHeaders: { Authorization: `Bearer ${accessToken}` },
            }),
          ),
        ),
        catchError((refreshError: unknown) => {
          authService.handleExpiredSession();
          return throwError(() => refreshError);
        }),
      ) as Observable<never>;
    }),
  );
};
