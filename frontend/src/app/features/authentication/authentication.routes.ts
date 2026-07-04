import { Routes } from '@angular/router';

import { guestGuard } from '../../core/guards/guest.guard';

/** Public routes, mounted at the application root without the authenticated shell. */
export const AUTHENTICATION_ROUTES: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./pages/login-page/login-page.component').then((m) => m.LoginPageComponent),
    title: 'Login - DeutschLingoDeck',
  },
  {
    path: 'register',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./pages/register-page/register-page.component').then(
        (m) => m.RegisterPageComponent,
      ),
    title: 'Register - DeutschLingoDeck',
  },
];
