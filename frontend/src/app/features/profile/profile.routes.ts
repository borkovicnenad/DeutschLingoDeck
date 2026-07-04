import { Routes } from '@angular/router';

/** Mounted at /profile by the root router. */
export const PROFILE_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/profile-page/profile-page.component').then((m) => m.ProfilePageComponent),
    title: 'Profile - DeutschLingoDeck',
  },
];
