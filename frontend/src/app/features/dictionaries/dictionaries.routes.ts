import { Routes } from '@angular/router';

/** Mounted at /dictionaries by the root router. */
export const DICTIONARIES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/dictionary-list-page/dictionary-list-page.component').then(
        (m) => m.DictionaryListPageComponent,
      ),
    title: 'Dictionaries - DeutschLingoDeck',
  },
  {
    path: 'import',
    loadComponent: () =>
      import('./pages/dictionary-import-page/dictionary-import-page.component').then(
        (m) => m.DictionaryImportPageComponent,
      ),
    title: 'Import Dictionary - DeutschLingoDeck',
  },
  {
    path: ':dictionaryId/cards/:cardId',
    loadComponent: () =>
      import('./pages/card-details-page/card-details-page.component').then(
        (m) => m.CardDetailsPageComponent,
      ),
    title: 'Card Details - DeutschLingoDeck',
  },
  {
    path: ':dictionaryId',
    loadComponent: () =>
      import('./pages/dictionary-details-page/dictionary-details-page.component').then(
        (m) => m.DictionaryDetailsPageComponent,
      ),
    title: 'Dictionary Details - DeutschLingoDeck',
  },
];
