import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';
import { AUTHENTICATION_ROUTES } from './features/authentication/authentication.routes';

export const routes: Routes = [
  ...AUTHENTICATION_ROUTES,

  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./layout/app-shell/app-shell.component').then((m) => m.AppShellComponent),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        loadChildren: () =>
          import('./features/dashboard/dashboard.routes').then((m) => m.DASHBOARD_ROUTES),
      },
      {
        path: 'dictionaries',
        loadChildren: () =>
          import('./features/dictionaries/dictionaries.routes').then(
            (m) => m.DICTIONARIES_ROUTES,
          ),
      },
      {
        path: 'game',
        loadChildren: () => import('./features/games/games.routes').then((m) => m.GAME_ROUTES),
      },
      {
        path: 'games',
        loadChildren: () =>
          import('./features/games/games.routes').then((m) => m.GAME_HISTORY_ROUTES),
      },
      {
        path: 'statistics',
        loadChildren: () =>
          import('./features/statistics/statistics.routes').then((m) => m.STATISTICS_ROUTES),
      },
      {
        path: 'profile',
        loadChildren: () =>
          import('./features/profile/profile.routes').then((m) => m.PROFILE_ROUTES),
      },
    ],
  },

  {
    path: 'access-denied',
    loadComponent: () =>
      import('./features/errors/pages/access-denied-page/access-denied-page.component').then(
        (m) => m.AccessDeniedPageComponent,
      ),
    title: 'Access Denied - DeutschLingoDeck',
  },
  {
    path: 'not-found',
    loadComponent: () =>
      import('./features/errors/pages/not-found-page/not-found-page.component').then(
        (m) => m.NotFoundPageComponent,
      ),
    title: 'Not Found - DeutschLingoDeck',
  },
  { path: '**', redirectTo: 'not-found' },
];
