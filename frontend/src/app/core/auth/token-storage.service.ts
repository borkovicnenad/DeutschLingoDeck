import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

const ACCESS_TOKEN_KEY = 'dld_access_token';
const REFRESH_TOKEN_KEY = 'dld_refresh_token';

/**
 * Wraps browser storage for authentication tokens.
 *
 * TODO: Re-evaluate storage strategy before production (e.g. refresh token in an
 * HttpOnly cookie) - localStorage is used here as a placeholder for the MVP.
 */
@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  getAccessToken(): string | null {
    return this.isBrowser ? localStorage.getItem(ACCESS_TOKEN_KEY) : null;
  }

  getRefreshToken(): string | null {
    return this.isBrowser ? localStorage.getItem(REFRESH_TOKEN_KEY) : null;
  }

  setTokens(accessToken: string, refreshToken: string): void {
    if (!this.isBrowser) {
      return;
    }
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  }

  clear(): void {
    if (!this.isBrowser) {
      return;
    }
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
  }
}
