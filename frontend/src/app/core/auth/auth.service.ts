import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, map, of, switchMap, tap } from 'rxjs';

import { User } from '../models/user.model';
import { AuthApiService, LoginRequest, RegisterRequest } from './auth-api.service';
import { TokenStorageService } from './token-storage.service';

/**
 * Holds global authentication state (current user, authentication status) and
 * orchestrates login/registration/logout/session restoration.
 *
 * TODO: This is a structural placeholder - it does not implement JWT expiry
 * inspection or refresh scheduling. See RefreshTokenInterceptor for the
 * automatic-refresh-on-401 flow.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly authApi = inject(AuthApiService);
  private readonly tokenStorage = inject(TokenStorageService);
  private readonly router = inject(Router);

  private readonly currentUserSignal = signal<User | null>(null);

  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isAuthenticated = computed(() => this.currentUserSignal() !== null);

  login(request: LoginRequest): Observable<User> {
    return this.authApi.login(request).pipe(
      tap((response) => this.applyAuthResponse(response)),
      map((response) => response.user),
    );
  }

  register(request: RegisterRequest): Observable<User> {
    return this.authApi.register(request).pipe(
      tap((response) => this.applyAuthResponse(response)),
      map((response) => response.user),
    );
  }

  logout(): void {
    this.authApi
      .logout()
      .pipe(catchError(() => of(void 0)))
      .subscribe(() => {
        this.clearSession();
        this.router.navigateByUrl('/login');
      });
  }

  /**
   * Restores the session on application bootstrap by exchanging a stored
   * refresh/access token pair for the current user. Resolves to `false`
   * without throwing when no valid session exists.
   */
  restoreSession(): Observable<boolean> {
    if (!this.tokenStorage.getAccessToken()) {
      return of(false);
    }

    return this.authApi.me().pipe(
      tap((user) => this.currentUserSignal.set(user)),
      map(() => true),
      catchError(() => {
        this.refreshAccessToken().pipe(
          switchMap(() => this.authApi.me()),
          tap((user) => this.currentUserSignal.set(user)),
          map(() => true),
          catchError(() => {
            this.clearSession();
            return of(false);
          })
        )
        this.clearSession();
        return of(false);
      }),
    );
  }

  /** Used by the Refresh Token Interceptor when a request fails with 401. */
  refreshAccessToken(): Observable<string> {
    const refreshToken = this.tokenStorage.getRefreshToken();
    if (!refreshToken) {
      this.clearSession();
      throw new Error('No refresh token available');
    }

    return this.authApi.refresh({ refreshToken }).pipe(
      tap((response) => this.applyAuthResponse(response)),
      map((response) => response.accessToken),
    );
  }

  /** Keeps the global current-user state in sync after a profile update. */
  updateDisplayName(displayName: string): void {
    this.currentUserSignal.update((user) => (user ? { ...user, displayName } : user));
  }

  /** Called when both access and refresh tokens are invalid/expired. */
  handleExpiredSession(): void {
    this.clearSession();
    this.router.navigateByUrl('/login');
    // TODO: surface a "session expired" notification once a notification
    // mechanism is introduced (UC-041).
  }

  private applyAuthResponse(response: {
    accessToken: string;
    refreshToken: string;
    user: User;
  }): void {
    this.tokenStorage.setTokens(response.accessToken, response.refreshToken);
    this.currentUserSignal.set(response.user);
  }

  private clearSession(): void {
    this.tokenStorage.clear();
    this.currentUserSignal.set(null);
  }
}
