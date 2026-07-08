import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
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
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Router } from '@angular/router';
import { map, timer } from 'rxjs';

import { BreadcrumbsComponent } from '../../../../shared/components/breadcrumbs/breadcrumbs.component';
import { InlineAlertComponent } from '../../../../shared/components/inline-alert/inline-alert.component';
import { ConfirmDialogService } from '../../../../shared/dialogs/confirm-dialog/confirm-dialog.service';
import { GameApiService } from '../../services/game-api.service';
import { ActiveGameStateService } from '../../state/active-game-state.service';

@Component({
  selector: 'app-active-game-page',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
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
export class ActiveGamePageComponent implements OnInit {
  protected readonly state = inject(ActiveGameStateService);
  private readonly gameApi = inject(GameApiService);
  private readonly confirmDialog = inject(ConfirmDialogService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly router = inject(Router);

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

  private readonly continueButton = viewChild<HTMLButtonElement>('continueButton');

  constructor() {
    // Auto-focus Continue when it appears so native Enter-on-button already matches a click.
    afterRenderEffect(() => {
      if (this.state.lastValidation()) {
        this.continueButton()?.focus();
      }
    });
  }

  ngOnInit(): void {
    this.state.load(this.gameIdAsNumber());
  }

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
