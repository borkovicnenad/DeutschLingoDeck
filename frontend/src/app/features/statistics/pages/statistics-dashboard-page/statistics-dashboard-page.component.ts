import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';

import { EmptyStateComponent } from '../../../../shared/components/empty-state/empty-state.component';
import { InlineAlertComponent } from '../../../../shared/components/inline-alert/inline-alert.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { StatCardComponent } from '../../../../shared/components/stat-card/stat-card.component';
import {
  CardStatistics,
  DashboardStatistics,
  DictionaryStatistics,
  LearningHistoryEntry,
} from '../../models/statistics.model';
import {
  SAMPLE_CARD_STATISTICS,
  SAMPLE_DASHBOARD_STATISTICS,
  SAMPLE_DICTIONARY_STATISTICS,
  SAMPLE_LEARNING_HISTORY,
} from '../../statistics.sample-data';
import { StatisticsApiService } from '../../services/statistics-api.service';

@Component({
  selector: 'app-statistics-dashboard-page',
  imports: [
    DatePipe,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressBarModule,
    MatTableModule,
    PageHeaderComponent,
    EmptyStateComponent,
    InlineAlertComponent,
    StatCardComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './statistics-dashboard-page.component.html',
  styleUrl: './statistics-dashboard-page.component.css',
})
export class StatisticsDashboardPageComponent {
  private readonly statisticsApi = inject(StatisticsApiService);

  protected readonly fromDate = signal<string>('');
  protected readonly toDate = signal<string>('');

  protected readonly overview = signal<DashboardStatistics | null>(SAMPLE_DASHBOARD_STATISTICS);
  protected readonly overviewLoading = signal(false);
  protected readonly overviewError = signal(false);
  protected readonly overviewUsingSampleData = signal(true);

  protected readonly dictionaryStatistics = signal<DictionaryStatistics[]>(
    SAMPLE_DICTIONARY_STATISTICS,
  );
  protected readonly dictionaryStatisticsLoading = signal(false);
  protected readonly dictionaryStatisticsError = signal(false);
  protected readonly dictionaryStatisticsUsingSampleData = signal(true);

  protected readonly cardStatistics = signal<CardStatistics[]>(SAMPLE_CARD_STATISTICS);
  protected readonly cardStatisticsLoading = signal(false);
  protected readonly cardStatisticsError = signal(false);
  protected readonly cardStatisticsUsingSampleData = signal(true);

  protected readonly learningHistory = signal<LearningHistoryEntry[]>(SAMPLE_LEARNING_HISTORY);
  protected readonly learningHistoryLoading = signal(false);
  protected readonly learningHistoryError = signal(false);
  protected readonly learningHistoryUsingSampleData = signal(true);

  protected readonly dictionaryColumns = [
    'dictionaryName',
    'completedGames',
    'averageAccuracy',
    'masteredCards',
    'difficultCards',
  ];
  protected readonly cardColumns = ['sourceText', 'timesShown', 'successRate', 'lastShownAt'];
  protected readonly historyColumns = ['dictionaryName', 'status', 'accuracy', 'startedAt'];

  constructor() {
    this.loadAll();
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
          this.overviewUsingSampleData.set(false);
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
        this.dictionaryStatisticsUsingSampleData.set(false);
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
        this.cardStatisticsUsingSampleData.set(false);
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
          this.learningHistoryUsingSampleData.set(false);
          this.learningHistoryLoading.set(false);
        },
        error: () => {
          this.learningHistoryError.set(true);
          this.learningHistoryLoading.set(false);
        },
      });
  }
}
