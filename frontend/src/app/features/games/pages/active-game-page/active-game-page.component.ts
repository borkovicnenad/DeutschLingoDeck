import { isPlatformBrowser } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  PLATFORM_ID,
  afterRenderEffect,
  computed,
  inject,
  input,
  signal,
  viewChild,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Router } from '@angular/router';
import { map, timer } from 'rxjs';

import { TokenStorageService } from '../../../../core/auth/token-storage.service';
import { API_BASE_URL } from '../../../../core/config/api-base-url.token';
import { BreadcrumbsComponent } from '../../../../shared/components/breadcrumbs/breadcrumbs.component';
import { InlineAlertComponent } from '../../../../shared/components/inline-alert/inline-alert.component';
import { ConfirmDialogService } from '../../../../shared/dialogs/confirm-dialog/confirm-dialog.service';
import { questionView as buildQuestionView, revealView as buildRevealView } from '../../models/card-presentation.util';
import { GameApiService } from '../../services/game-api.service';
import { ActiveGameStateService } from '../../state/active-game-state.service';

@Component({
  selector: 'app-active-game-page',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    BreadcrumbsComponent,
    InlineAlertComponent,
  ],
  providers: [ActiveGameStateService],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './active-game-page.component.html',
  styleUrl: './active-game-page.component.css',
})
export class ActiveGamePageComponent implements OnInit, OnDestroy {
  protected readonly state = inject(ActiveGameStateService);
  private readonly gameApi = inject(GameApiService);
  private readonly confirmDialog = inject(ConfirmDialogService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly tokenStorage = inject(TokenStorageService);
  private readonly baseUrl = inject(API_BASE_URL);
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  readonly gameId = input.required<string>();
  private readonly gameIdAsNumber = computed(() => Number(this.gameId()));

  protected readonly answerForm = this.formBuilder.nonNullable.group({
    answer: ['', [Validators.required, Validators.maxLength(500)]],
  });

  protected readonly elapsedSeconds = toSignal(
    timer(0, 1000).pipe(map((tick) => tick)),
    { initialValue: 0 },
  );

  protected readonly finishing = signal(false);

  protected readonly question = computed(() => {
    const card = this.state.game()?.currentCard;
    return card ? buildQuestionView(card) : null;
  });

  protected readonly reveal = computed(() => {
    const card = this.state.game()?.currentCard;
    const revealedCard = this.state.lastValidation()?.revealedCard;
    return card && revealedCard ? buildRevealView(card, revealedCard) : null;
  });

  private readonly continueButton = viewChild<HTMLButtonElement>('continueButton');
  private readonly answerInput = viewChild<ElementRef<HTMLInputElement>>('answerInput');

  constructor() {
    afterRenderEffect(() => {
      if (this.state.lastValidation()) {
        // Auto-focus Continue when it appears so native Enter-on-button already matches a click.
        this.continueButton()?.focus();
      } else if (this.state.game()?.currentCard) {
        // Auto-focus the answer input for every new card (initial load and Continue alike).
        this.answerInput()?.nativeElement.focus();
      }
    });
  }

  ngOnInit(): void {
    this.state.load(this.gameIdAsNumber());
    if (this.isBrowser) {
      window.addEventListener('pagehide', this.handlePageHide);
    }
  }

  ngOnDestroy(): void {
    if (this.isBrowser) {
      window.removeEventListener('pagehide', this.handlePageHide);
    }
  }

  /**
   * Best-effort abandon on browser refresh/close/navigate-away. `pagehide` (unlike
   * `visibilitychange`) doesn't fire on same-document Angular route changes or on a tab-switch,
   * so this only triggers on an actual unload - exactly the case the client can catch that
   * `GameLifecycleReaper` otherwise has to wait out an idle timeout for. `fetch` with
   * `keepalive: true` is used instead of `HttpClient` because the request must be able to
   * survive the page unloading before it completes.
   */
  private readonly handlePageHide = (): void => {
    const game = this.state.game();
    if (!game || game.status !== 'IN_PROGRESS') {
      return;
    }
    const token = this.tokenStorage.getAccessToken();
    if (!token) {
      return;
    }
    fetch(`${this.baseUrl}/games/${this.gameIdAsNumber()}/abandon`, {
      method: 'POST',
      keepalive: true,
      headers: { Authorization: `Bearer ${token}` },
    }).catch(() => {
      // Best-effort - GameLifecycleReaper is the backstop if this doesn't land.
    });
  };

  protected retryLoad(): void {
    this.state.load(this.gameIdAsNumber());
  }

  protected submitAnswer(): void {
    const currentCard = this.state.game()?.currentCard;
    if (this.answerForm.invalid || !currentCard) {
      this.answerForm.markAllAsTouched();
      return;
    }

    this.state.submitAnswer(
      this.gameIdAsNumber(),
      currentCard.id,
      this.answerForm.getRawValue().answer,
    );
  }

  protected skipAnswer(): void {
    const currentCard = this.state.game()?.currentCard;
    if (!currentCard || this.state.submitting()) {
      return;
    }

    // Skip intentionally bypasses the Submit button's required validation - it always
    // records an empty answer, regardless of whatever partial text is in the field.
    this.state.submitAnswer(this.gameIdAsNumber(), currentCard.id, '');
  }

  protected continue(): void {
    this.state.continueToNextCard();
    this.answerForm.reset({ answer: '' });
  }

  protected onFeedbackKeydownEnter(event: Event): void {
    // preventDefault stops the focused button's native Enter-triggers-click, avoiding a double continue().
    event.preventDefault();
    this.continue();
  }

  protected finishGame(): void {
    this.finishing.set(true);
    this.gameApi.finish(this.gameIdAsNumber()).subscribe(() => {
      this.router.navigate(['/game', this.gameIdAsNumber(), 'summary']);
    });
  }

  protected abandonGame(): void {
    this.confirmDialog
      .confirm({
        title: 'Abandon game',
        message: 'Your progress so far will be kept, but this session will be marked as abandoned.',
        confirmLabel: 'Abandon',
      })
      .subscribe((confirmed) => {
        if (!confirmed) {
          return;
        }

        this.gameApi.abandon(this.gameIdAsNumber()).subscribe(() => {
          this.router.navigateByUrl('/dashboard');
        });
      });
  }
}
