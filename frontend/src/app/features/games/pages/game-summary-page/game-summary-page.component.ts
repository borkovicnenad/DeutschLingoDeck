import { ChangeDetectionStrategy, Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Router, RouterLink } from '@angular/router';

import { BreadcrumbsComponent } from '../../../../shared/components/breadcrumbs/breadcrumbs.component';
import { InlineAlertComponent } from '../../../../shared/components/inline-alert/inline-alert.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { StatCardComponent } from '../../../../shared/components/stat-card/stat-card.component';
import { AppError } from '../../../../shared/models/api-error.model';
import { DurationPipe } from '../../../../shared/pipes/duration.pipe';
import { GameSummary } from '../../models/game.model';
import { GameApiService } from '../../services/game-api.service';

@Component({
  selector: 'app-game-summary-page',
  imports: [
    RouterLink,
    MatButtonModule,
    MatProgressBarModule,
    DurationPipe,
    PageHeaderComponent,
    BreadcrumbsComponent,
    InlineAlertComponent,
    StatCardComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './game-summary-page.component.html',
  styleUrl: './game-summary-page.component.css',
})
export class GameSummaryPageComponent implements OnInit {
  private readonly gameApi = inject(GameApiService);
  private readonly router = inject(Router);

  readonly gameId = input.required<string>();
  private readonly gameIdAsNumber = computed(() => Number(this.gameId()));

  protected readonly summary = signal<GameSummary | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal<AppError | null>(null);

  ngOnInit(): void {
    this.load();
  }

  protected load(): void {
    this.loading.set(true);
    this.error.set(null);

    this.gameApi.getSummary(this.gameIdAsNumber()).subscribe({
      next: (summary) => {
        this.summary.set(summary);
        this.loading.set(false);
      },
      error: (error: AppError) => {
        this.error.set(error);
        this.loading.set(false);
      },
    });
  }

  protected playAgain(): void {
    this.router.navigateByUrl('/game/start');
  }
}
