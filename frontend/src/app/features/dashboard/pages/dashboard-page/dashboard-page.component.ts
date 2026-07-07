import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { AuthService } from '../../../../core/auth/auth.service';
import { EmptyStateComponent } from '../../../../shared/components/empty-state/empty-state.component';
import { InlineAlertComponent } from '../../../../shared/components/inline-alert/inline-alert.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { StatCardComponent } from '../../../../shared/components/stat-card/stat-card.component';
import { DictionarySummary } from '../../../dictionaries/models/dictionary.model';
import { DictionaryApiService } from '../../../dictionaries/services/dictionary-api.service';
import { Achievement, DailyGoal, LevelProgress } from '../../../gamification/models/gamification.model';
import { GamificationService } from '../../../gamification/services/gamification.service';
import { Game } from '../../../games/models/game.model';
import { GameApiService } from '../../../games/services/game-api.service';
import { DashboardStatistics } from '../../../statistics/models/statistics.model';
import { StatisticsApiService } from '../../../statistics/services/statistics-api.service';

@Component({
  selector: 'app-dashboard-page',
  imports: [
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressBarModule,
    MatTooltipModule,
    PageHeaderComponent,
    EmptyStateComponent,
    InlineAlertComponent,
    StatCardComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './dashboard-page.component.html',
  styleUrl: './dashboard-page.component.css',
})
export class DashboardPageComponent {
  protected readonly authService = inject(AuthService);
  private readonly statisticsApi = inject(StatisticsApiService);
  private readonly dictionaryApi = inject(DictionaryApiService);
  private readonly gameApi = inject(GameApiService);
  private readonly gamificationApi = inject(GamificationService);

  protected readonly statistics = signal<DashboardStatistics | null>(null);
  protected readonly statisticsLoading = signal(true);
  protected readonly statisticsError = signal(false);

  protected readonly dictionaries = signal<DictionarySummary[]>([]);
  protected readonly dictionariesLoading = signal(true);
  protected readonly dictionariesError = signal(false);

  protected readonly activeGame = signal<Game | null>(null);

  protected readonly levelProgress = signal<LevelProgress | null>(null);
  protected readonly dailyGoal = signal<DailyGoal | null>(null);
  protected readonly achievements = signal<Achievement[]>([]);
  protected readonly gamificationLoading = signal(true);
  protected readonly gamificationError = signal(false);

  constructor() {
    this.loadStatistics();
    this.loadDictionaries();
    this.loadActiveGame();
    this.loadGamification();
  }

  protected loadStatistics(): void {
    this.statisticsLoading.set(true);
    this.statisticsError.set(false);

    this.statisticsApi.getDashboardStatistics().subscribe({
      next: (statistics) => {
        this.statistics.set(statistics);
        this.statisticsLoading.set(false);
      },
      error: () => {
        this.statisticsError.set(true);
        this.statisticsLoading.set(false);
      },
    });
  }

  protected loadDictionaries(): void {
    this.dictionariesLoading.set(true);
    this.dictionariesError.set(false);

    this.dictionaryApi.list(0, 5).subscribe({
      next: (response) => {
        this.dictionaries.set(response.content);
        this.dictionariesLoading.set(false);
      },
      error: () => {
        this.dictionariesError.set(true);
        this.dictionariesLoading.set(false);
      },
    });
  }

  private loadActiveGame(): void {
    this.gameApi.findActiveGame().subscribe({
      next: (game) => this.activeGame.set(game),
      error: () => {},
    });
  }

  protected loadGamification(): void {
    this.gamificationLoading.set(true);
    this.gamificationError.set(false);

    forkJoin({
      levelProgress: this.gamificationApi.getLevelProgress(),
      dailyGoal: this.gamificationApi.getDailyGoal(),
      achievements: this.gamificationApi.getAchievements(),
    }).subscribe({
      next: ({ levelProgress, dailyGoal, achievements }) => {
        this.levelProgress.set(levelProgress);
        this.dailyGoal.set(dailyGoal);
        this.achievements.set(achievements.slice(0, 5));
        this.gamificationLoading.set(false);
      },
      error: () => {
        this.gamificationError.set(true);
        this.gamificationLoading.set(false);
      },
    });
  }

  protected xpPercentage(progress: LevelProgress): number {
    return Math.min(100, (progress.currentXp / progress.xpToNextLevel) * 100);
  }

  protected goalPercentage(goal: DailyGoal): number {
    return Math.min(100, (goal.completedCards / goal.targetCards) * 100);
  }
}
