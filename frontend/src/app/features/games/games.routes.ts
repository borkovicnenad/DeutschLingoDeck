import { Routes } from '@angular/router';

/** Mounted at /game by the root router (start / active / summary screens). */
export const GAME_ROUTES: Routes = [
  {
    path: 'start',
    loadComponent: () =>
      import('./pages/start-game-page/start-game-page.component').then(
        (m) => m.StartGamePageComponent,
      ),
    title: 'Start Game - DeutschLingoDeck',
  },
  {
    path: ':gameId/summary',
    loadComponent: () =>
      import('./pages/game-summary-page/game-summary-page.component').then(
        (m) => m.GameSummaryPageComponent,
      ),
    title: 'Game Summary - DeutschLingoDeck',
  },
  {
    path: ':gameId',
    loadComponent: () =>
      import('./pages/active-game-page/active-game-page.component').then(
        (m) => m.ActiveGamePageComponent,
      ),
    title: 'Game - DeutschLingoDeck',
  },
];

/** Mounted at /games by the root router (game history screen). */
export const GAME_HISTORY_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/game-history-page/game-history-page.component').then(
        (m) => m.GameHistoryPageComponent,
      ),
    title: 'Game History - DeutschLingoDeck',
  },
];
