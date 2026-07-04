import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';

import { BadgeComponent } from '../../../../shared/components/badge/badge.component';
import { EmptyStateComponent } from '../../../../shared/components/empty-state/empty-state.component';
import { InlineAlertComponent } from '../../../../shared/components/inline-alert/inline-alert.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { AppError } from '../../../../shared/models/api-error.model';
import { SAMPLE_GAME_HISTORY } from '../../games.sample-data';
import { GameStatus } from '../../models/game-status.enum';
import { GameSummary } from '../../models/game.model';
import { GameApiService } from '../../services/game-api.service';

@Component({
  selector: 'app-game-history-page',
  imports: [
    FormsModule,
    RouterLink,
    MatFormFieldModule,
    MatSelectModule,
    MatTableModule,
    MatPaginatorModule,
    MatProgressBarModule,
    PageHeaderComponent,
    EmptyStateComponent,
    InlineAlertComponent,
    BadgeComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './game-history-page.component.html',
})
export class GameHistoryPageComponent {
  private readonly gameApi = inject(GameApiService);

  protected readonly games = signal<GameSummary[]>(SAMPLE_GAME_HISTORY);
  protected readonly loading = signal(false);
  protected readonly error = signal<AppError | null>(null);
  protected readonly usingSampleData = signal(true);
  protected readonly page = signal(0);
  protected readonly size = signal(20);
  protected readonly totalElements = signal(SAMPLE_GAME_HISTORY.length);
  protected readonly statusFilter = signal<GameStatus | ''>('');

  protected readonly statuses: GameStatus[] = [
    'CREATED',
    'STARTED',
    'IN_PROGRESS',
    'FINISHED',
    'ABANDONED',
  ];
  protected readonly displayedColumns = ['gameId', 'status', 'accuracy', 'duration', 'actions'];

  constructor() {
    this.load();
  }

  protected load(page = this.page(), size = this.size()): void {
    this.loading.set(true);
    this.error.set(null);

    this.gameApi
      .list({ page, size, status: this.statusFilter() || undefined })
      .subscribe({
        next: (response) => {
          this.games.set(response.content);
          this.page.set(response.page);
          this.size.set(response.size);
          this.totalElements.set(response.totalElements);
          this.usingSampleData.set(false);
          this.loading.set(false);
        },
        error: (error: AppError) => {
          this.error.set(error);
          this.loading.set(false);
        },
      });
  }

  protected onStatusFilterChange(status: GameStatus | ''): void {
    this.statusFilter.set(status);
    this.load(0);
  }

  protected onPage(event: PageEvent): void {
    this.load(event.pageIndex, event.pageSize);
  }
}
