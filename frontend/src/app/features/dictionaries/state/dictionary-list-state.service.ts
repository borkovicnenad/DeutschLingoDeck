import { Injectable, computed, inject, signal } from '@angular/core';
import { Sort } from '@angular/material/sort';

import { AppError } from '../../../shared/models/api-error.model';
import { DictionarySummary } from '../models/dictionary.model';
import { DictionaryApiService } from '../services/dictionary-api.service';

function compareValues(a: unknown, b: unknown): number {
  if (typeof a === 'number' && typeof b === 'number') {
    return a - b;
  }
  return String(a).localeCompare(String(b));
}

/** Local reactive state for the Dictionary List page. */
@Injectable()
export class DictionaryListStateService {
  private readonly dictionaryApi = inject(DictionaryApiService);

  private readonly dictionariesSignal = signal<DictionarySummary[]>([]);
  private readonly loadingSignal = signal(false);
  private readonly errorSignal = signal<AppError | null>(null);
  private readonly pageSignal = signal(0);
  private readonly sizeSignal = signal(20);
  private readonly totalElementsSignal = signal(0);
  private readonly searchTermSignal = signal('');
  private readonly sortSignal = signal<Sort>({ active: '', direction: '' });

  readonly loading = this.loadingSignal.asReadonly();
  readonly error = this.errorSignal.asReadonly();
  readonly page = this.pageSignal.asReadonly();
  readonly size = this.sizeSignal.asReadonly();
  readonly totalElements = this.totalElementsSignal.asReadonly();
  readonly searchTerm = this.searchTermSignal.asReadonly();
  readonly sort = this.sortSignal.asReadonly();

  /**
   * Filters and sorts the currently loaded page client-side. The backend
   * does not expose a name search parameter, so both are limited to the
   * loaded page.
   */
  readonly dictionaries = computed(() => {
    const term = this.searchTermSignal().trim().toLowerCase();
    const filtered = term
      ? this.dictionariesSignal().filter((d) => d.name.toLowerCase().includes(term))
      : this.dictionariesSignal();

    const sort = this.sortSignal();
    if (!sort.active || sort.direction === '') {
      return filtered;
    }

    const factor = sort.direction === 'asc' ? 1 : -1;
    const active = sort.active as keyof DictionarySummary;
    return [...filtered].sort((a, b) => factor * compareValues(a[active], b[active]));
  });

  readonly isEmpty = computed(
    () => !this.loadingSignal() && !this.errorSignal() && this.dictionariesSignal().length === 0,
  );

  /** True when a search term is active but matches nothing on the loaded page. */
  readonly hasNoSearchResults = computed(
    () => !this.isEmpty() && this.searchTermSignal().trim().length > 0 && this.dictionaries().length === 0,
  );

  load(page = this.pageSignal(), size = this.sizeSignal()): void {
    this.loadingSignal.set(true);
    this.errorSignal.set(null);

    this.dictionaryApi.list(page, size).subscribe({
      next: (response) => {
        this.dictionariesSignal.set(response.content);
        this.pageSignal.set(response.page);
        this.sizeSignal.set(response.size);
        this.totalElementsSignal.set(response.totalElements);
        this.loadingSignal.set(false);
      },
      error: (error: AppError) => {
        this.errorSignal.set(error);
        this.loadingSignal.set(false);
      },
    });
  }

  setSearchTerm(term: string): void {
    this.searchTermSignal.set(term);
  }

  setSort(sort: Sort): void {
    this.sortSignal.set(sort);
  }

  removeLocally(dictionaryId: number): void {
    this.dictionariesSignal.update((dictionaries) =>
      dictionaries.filter((d) => d.id !== dictionaryId),
    );
  }
}
