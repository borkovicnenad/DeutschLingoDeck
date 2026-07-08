import { Injectable, computed, inject, signal } from '@angular/core';

import { AppError } from '../../../shared/models/api-error.model';
import { AnswerValidationResponse, CurrentCard, Game } from '../models/game.model';
import { GameApiService } from '../services/game-api.service';

/**
 * Answering: no validation yet, current card accepts input.
 * Reviewing: validation is shown for the current card; the next card is
 * staged but must not be displayed until the user continues.
 */
export type GamePhase = 'answering' | 'reviewing';

/** Local reactive state for the Active Game page. */
@Injectable()
export class ActiveGameStateService {
  private readonly gameApi = inject(GameApiService);

  private readonly gameSignal = signal<Game | null>(null);
  private readonly loadingSignal = signal(false);
  private readonly errorSignal = signal<AppError | null>(null);
  private readonly submittingSignal = signal(false);
  private readonly lastValidationSignal = signal<AnswerValidationResponse | null>(null);
  // Holds the next card once it's known, without displaying it — it is only
  // merged into `game.currentCard` when continueToNextCard() runs.
  private readonly pendingNextCardSignal = signal<CurrentCard | undefined>(undefined);
  private questionStartedAt = Date.now();

  readonly game = this.gameSignal.asReadonly();
  readonly loading = this.loadingSignal.asReadonly();
  readonly error = this.errorSignal.asReadonly();
  readonly submitting = this.submittingSignal.asReadonly();
  readonly lastValidation = this.lastValidationSignal.asReadonly();
  readonly phase = computed<GamePhase>(() => (this.lastValidationSignal() ? 'reviewing' : 'answering'));

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
        this.pendingNextCardSignal.set(validation.nextCard);
        this.gameSignal.update((game) =>
          game
            ? {
                ...game,
                answeredCards: game.answeredCards + 1,
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
    const nextCard = this.pendingNextCardSignal();
    this.gameSignal.update((game) => (game ? { ...game, currentCard: nextCard } : game));
    this.pendingNextCardSignal.set(undefined);
    this.lastValidationSignal.set(null);
    this.questionStartedAt = Date.now();
  }
}
