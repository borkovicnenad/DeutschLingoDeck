import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';

import { ChartBarComponent } from '../../../../shared/components/chart-bar/chart-bar.component';
import { ChartDonutComponent, DonutChartSegment } from '../../../../shared/components/chart-donut/chart-donut.component';
import { ChartLineComponent, LineChartPoint } from '../../../../shared/components/chart-line/chart-line.component';
import { EmptyStateComponent } from '../../../../shared/components/empty-state/empty-state.component';
import { InlineAlertComponent } from '../../../../shared/components/inline-alert/inline-alert.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { StatCardComponent } from '../../../../shared/components/stat-card/stat-card.component';
import { WeeklyActivityPoint } from '../../../gamification/models/gamification.model';
import { GamificationService } from '../../../gamification/services/gamification.service';
import {
  CardStatistics,
  DashboardStatistics,
  DictionaryStatistics,
  LearningHistoryEntry,
} from '../../models/statistics.model';
import { StatisticsApiService } from '../../services/statistics-api.service';

const MONTH_ABBR = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

/** UTC-based (not locale-based) so the label is identical between SSR and client hydration. */
function formatShortDate(iso: string): string {
  const date = new Date(iso);
  return `${MONTH_ABBR[date.getUTCMonth()]} ${date.getUTCDate()}`;
}

function compareValues(a: unknown, b: unknown): number {
  if (a == null && b == null) {
    return 0;
  }
  if (a == null) {
    return -1;
  }
  if (b == null) {
    return 1;
  }
  if (typeof a === 'number' && typeof b === 'number') {
    return a - b;
  }
  return String(a).localeCompare(String(b));
}

function sortRows<T>(rows: T[], sort: Sort): T[] {
  if (!sort.active || sort.direction === '') {
    return rows;
  }
  const factor = sort.direction === 'asc' ? 1 : -1;
  return [...rows].sort(
    (a, b) => factor * compareValues((a as Record<string, unknown>)[sort.active], (b as Record<string, unknown>)[sort.active]),
  );
}

@Component({
  selector: 'app-statistics-dashboard-page',
  imports: [
    DatePipe,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressBarModule,
    MatSortModule,
    MatTableModule,
    PageHeaderComponent,
    EmptyStateComponent,
    InlineAlertComponent,
    StatCardComponent,
    ChartBarComponent,
    ChartLineComponent,
    ChartDonutComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './statistics-dashboard-page.component.html',
  styleUrl: './statistics-dashboard-page.component.css',
})
export class StatisticsDashboardPageComponent {
  private readonly statisticsApi = inject(StatisticsApiService);
  private readonly gamificationApi = inject(GamificationService);

  protected readonly weeklyActivity = signal<WeeklyActivityPoint[]>([]);
  protected readonly weeklyActivityError = signal(false);

  protected readonly fromDate = signal<string>('');
  protected readonly toDate = signal<string>('');

  protected readonly overview = signal<DashboardStatistics | null>(null);
  protected readonly overviewLoading = signal(false);
  protected readonly overviewError = signal(false);

  protected readonly dictionaryStatistics = signal<DictionaryStatistics[]>([]);
  protected readonly dictionaryStatisticsLoading = signal(false);
  protected readonly dictionaryStatisticsError = signal(false);

  protected readonly cardStatistics = signal<CardStatistics[]>([]);
  protected readonly cardStatisticsLoading = signal(false);
  protected readonly cardStatisticsError = signal(false);

  protected readonly learningHistory = signal<LearningHistoryEntry[]>([]);
  protected readonly learningHistoryLoading = signal(false);
  protected readonly learningHistoryError = signal(false);

  protected readonly dictionaryColumns = [
    'dictionaryName',
    'completedGames',
    'averageAccuracy',
    'masteredCards',
    'difficultCards',
  ];
  protected readonly cardColumns = ['sourceText', 'timesShown', 'successRate', 'lastShownAt'];
  protected readonly historyColumns = ['dictionaryName', 'status', 'accuracy', 'startedAt'];

  protected readonly dictionarySort = signal<Sort>({ active: '', direction: '' });
  protected readonly cardSort = signal<Sort>({ active: '', direction: '' });
  protected readonly historySort = signal<Sort>({ active: '', direction: '' });

  protected readonly sortedDictionaryStatistics = computed(() =>
    sortRows(this.dictionaryStatistics(), this.dictionarySort()),
  );
  protected readonly sortedCardStatistics = computed(() =>
    sortRows(this.cardStatistics(), this.cardSort()),
  );
  protected readonly sortedLearningHistory = computed(() =>
    sortRows(this.learningHistory(), this.historySort()),
  );

  protected readonly weeklyActivityChartData = computed(() =>
    this.weeklyActivity().map((point) => ({ label: point.day, value: point.cardsReviewed })),
  );

  protected readonly accuracyTrendChartData = computed<LineChartPoint[]>(() =>
    [...this.learningHistory()]
      .filter((entry) => entry.startedAt && entry.accuracy !== undefined)
      .sort((a, b) => new Date(a.startedAt!).getTime() - new Date(b.startedAt!).getTime())
      .map((entry) => ({
        label: formatShortDate(entry.startedAt!),
        value: Math.round((entry.accuracy ?? 0) * 100),
      })),
  );

  protected readonly masterySegments = computed<DonutChartSegment[]>(() => {
    const stats = this.dictionaryStatistics();
    return [
      {
        label: 'Mastered',
        value: stats.reduce((sum, s) => sum + (s.masteredCards ?? 0), 0),
        color: 'success',
      },
      {
        label: 'Difficult',
        value: stats.reduce((sum, s) => sum + (s.difficultCards ?? 0), 0),
        color: 'danger',
      },
    ];
  });

  constructor() {
    this.loadAll();
    this.loadWeeklyActivity();
  }

  protected loadWeeklyActivity(): void {
    this.weeklyActivityError.set(false);
    this.gamificationApi.getWeeklyActivity().subscribe({
      next: (activity) => this.weeklyActivity.set(activity),
      error: () => this.weeklyActivityError.set(true),
    });
  }

  protected onDictionarySort(sort: Sort): void {
    this.dictionarySort.set(sort);
  }

  protected onCardSort(sort: Sort): void {
    this.cardSort.set(sort);
  }

  protected onHistorySort(sort: Sort): void {
    this.historySort.set(sort);
  }

  protected loadAll(): void {
    this.loadOverview();
    this.loadDictionaryStatistics();
    this.loadCardStatistics();
    this.loadLearningHistory();
  }

  protected onDateFilterChange(): void {
    this.loadOverview();
    this.loadLearningHistory();
  }

  protected loadOverview(): void {
    this.overviewLoading.set(true);
    this.overviewError.set(false);
    this.statisticsApi
      .getDashboardStatistics({ from: this.fromDate() || undefined, to: this.toDate() || undefined })
      .subscribe({
        next: (overview) => {
          this.overview.set(overview);
          this.overviewLoading.set(false);
        },
        error: () => {
          this.overviewError.set(true);
          this.overviewLoading.set(false);
        },
      });
  }

  protected loadDictionaryStatistics(): void {
    this.dictionaryStatisticsLoading.set(true);
    this.dictionaryStatisticsError.set(false);
    this.statisticsApi.getDictionaryStatistics().subscribe({
      next: (statistics) => {
        this.dictionaryStatistics.set(statistics);
        this.dictionaryStatisticsLoading.set(false);
      },
      error: () => {
        this.dictionaryStatisticsError.set(true);
        this.dictionaryStatisticsLoading.set(false);
      },
    });
  }

  protected loadCardStatistics(): void {
    this.cardStatisticsLoading.set(true);
    this.cardStatisticsError.set(false);
    this.statisticsApi.getCardStatistics().subscribe({
      next: (response) => {
        this.cardStatistics.set(response.content);
        this.cardStatisticsLoading.set(false);
      },
      error: () => {
        this.cardStatisticsError.set(true);
        this.cardStatisticsLoading.set(false);
      },
    });
  }

  protected loadLearningHistory(): void {
    this.learningHistoryLoading.set(true);
    this.learningHistoryError.set(false);
    this.statisticsApi
      .getLearningHistory({ from: this.fromDate() || undefined, to: this.toDate() || undefined })
      .subscribe({
        next: (response) => {
          this.learningHistory.set(response.content);
          this.learningHistoryLoading.set(false);
        },
        error: () => {
          this.learningHistoryError.set(true);
          this.learningHistoryLoading.set(false);
        },
      });
  }
}
