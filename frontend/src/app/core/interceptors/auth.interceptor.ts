import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

import { TokenStorageService } from '../auth/token-storage.service';

const PUBLIC_ENDPOINTS = ['/auth/register', '/auth/login', '/auth/refresh'];

/** Automatically attaches the JWT access token to every authenticated request. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenStorage = inject(TokenStorageService);

  const isPublicEndpoint = PUBLIC_ENDPOINTS.some((endpoint) => req.url.includes(endpoint));
  const accessToken = tokenStorage.getAccessToken();

  if (isPublicEndpoint || !accessToken) {
    return next(req);
  }

  return next(
    req.clone({
      setHeaders: { Authorization: `Bearer ${accessToken}` },
    }),
  );
};
