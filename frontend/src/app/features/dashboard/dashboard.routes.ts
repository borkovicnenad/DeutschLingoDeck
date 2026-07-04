import { Routes } from '@angular/router';

/** Mounted at /dashboard by the root router. */
export const DASHBOARD_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/dashboard-page/dashboard-page.component').then(
        (m) => m.DashboardPageComponent,
      ),
    title: 'Dashboard - DeutschLingoDeck',
  },
];
