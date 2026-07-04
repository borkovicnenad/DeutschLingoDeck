import { Routes } from '@angular/router';

/** Mounted at /statistics by the root router. */
export const STATISTICS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/statistics-dashboard-page/statistics-dashboard-page.component').then(
        (m) => m.StatisticsDashboardPageComponent,
      ),
    title: 'Statistics - DeutschLingoDeck',
  },
];
