import { Injectable, computed, inject, signal } from '@angular/core';

import { AppError } from '../../../shared/models/api-error.model';
import { SAMPLE_ACTIVE_GAME } from '../games.sample-data';
import { AnswerValidationResponse, Game } from '../models/game.model';
import { GameApiService } from '../services/game-api.service';

/**
 * Local reactive state for the Active Game page.
 *
 * Starts out showing a static sample game so the page is never empty and is
 * fully navigable before/without a live backend. A successful load() always
 * replaces the sample data with the real game; a failed load() keeps the
 * sample game on screen and only surfaces `error` for a non-blocking notice.
 */
@Injectable()
export class ActiveGameStateService {
  private readonly gameApi = inject(GameApiService);

  private readonly gameSignal = signal<Game | null>(SAMPLE_ACTIVE_GAME);
  private readonly loadingSignal = signal(false);
  private readonly errorSignal = signal<AppError | null>(null);
  private readonly usingSampleDataSignal = signal(true);
  private readonly submittingSignal = signal(false);
  private readonly lastValidationSignal = signal<AnswerValidationResponse | null>(null);
  private questionStartedAt = Date.now();

  readonly game = this.gameSignal.asReadonly();
  readonly loading = this.loadingSignal.asReadonly();
  readonly error = this.errorSignal.asReadonly();
  readonly usingSampleData = this.usingSampleDataSignal.asReadonly();
  readonly submitting = this.submittingSignal.asReadonly();
  readonly lastValidation = this.lastValidationSignal.asReadonly();

  readonly progress = computed(() => {
    const game = this.gameSignal();
    if (!game || game.totalCards === 0) {
      return 0;
    }
    return Math.round((game.answeredCards / game.totalCards) * 100);
  });

  load(gameId: number): void {
    this.loadingSignal.set(true);
    this.errorSignal.set(null);

    this.gameApi.getById(gameId).subscribe({
      next: (game) => {
        this.gameSignal.set(game);
        this.usingSampleDataSignal.set(false);
        this.loadingSignal.set(false);
        this.questionStartedAt = Date.now();
      },
      error: (error: AppError) => {
        this.errorSignal.set(error);
        this.loadingSignal.set(false);
      },
    });
  }

  submitAnswer(gameId: number, cardId: number, answer: string): void {
    if (this.submittingSignal()) {
      return;
    }

    this.submittingSignal.set(true);
    const responseTimeMs = Date.now() - this.questionStartedAt;

    this.gameApi.submitAnswer(gameId, { cardId, answer, responseTimeMs }).subscribe({
      next: (validation) => {
        this.lastValidationSignal.set(validation);
        this.gameSignal.update((game) =>
          game
            ? {
                ...game,
                answeredCards: game.answeredCards + 1,
                currentCard: validation.nextCard,
              }
            : game,
        );
        this.submittingSignal.set(false);
        // questionStartedAt intentionally NOT reset here — the user is still
        // reading the validation feedback. It resets in continueToNextCard(),
        // when the next card is actually displayed, so responseTimeMs isn't
        // inflated by feedback-reading time.
      },
      error: (error: AppError) => {
        this.errorSignal.set(error);
        this.submittingSignal.set(false);
      },
    });
  }

  continueToNextCard(): void {
    this.lastValidationSignal.set(null);
    this.questionStartedAt = Date.now();
  }
}
