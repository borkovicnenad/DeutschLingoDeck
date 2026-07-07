import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { ActivatedRoute, Router } from '@angular/router';

import { DictionaryApiService } from '../../../dictionaries/services/dictionary-api.service';
import { DictionarySummary } from '../../../dictionaries/models/dictionary.model';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { AppError } from '../../../../shared/models/api-error.model';
import { Game } from '../../models/game.model';
import { GameApiService } from '../../services/game-api.service';

@Component({
  selector: 'app-start-game-page',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatSelectModule,
    MatIconModule,
    MatProgressBarModule,
    PageHeaderComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './start-game-page.component.html',
  styleUrl: './start-game-page.component.css',
})
export class StartGamePageComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly dictionaryApi = inject(DictionaryApiService);
  private readonly gameApi = inject(GameApiService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly dictionaries = signal<DictionarySummary[]>([]);
  protected readonly loadingDictionaries = signal(false);
  protected readonly starting = signal(false);
  protected readonly startError = signal<AppError | null>(null);
  protected readonly activeGame = signal<Game | null>(null);

  protected readonly form = this.formBuilder.nonNullable.group({
    dictionaryId: [null as number | null, [Validators.required]],
    gameMode: ['STANDARD', [Validators.required]],
  });

  constructor() {
    this.loadingDictionaries.set(true);
    this.dictionaryApi.list(0, 100).subscribe({
      next: (response) => {
        this.dictionaries.set(response.content);
        this.loadingDictionaries.set(false);
      },
      error: () => this.loadingDictionaries.set(false),
    });

    const preselectedDictionaryId = Number(this.route.snapshot.queryParamMap.get('dictionaryId'));
    if (preselectedDictionaryId) {
      this.form.patchValue({ dictionaryId: preselectedDictionaryId });
    }

    this.gameApi.findActiveGame().subscribe({
      next: (game) => this.activeGame.set(game),
      error: () => {},
    });
  }

  protected resumeGame(): void {
    const game = this.activeGame();
    if (game) {
      this.router.navigate(['/game', game.id]);
    }
  }

  protected submit(): void {
    if (this.form.invalid || this.starting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.starting.set(true);
    this.startError.set(null);

    const { dictionaryId, gameMode } = this.form.getRawValue();

    this.gameApi.start({ dictionaryId: dictionaryId!, gameMode }).subscribe({
      next: (game) => this.router.navigate(['/game', game.id]),
      error: (error: AppError) => {
        this.starting.set(false);
        this.startError.set(error);
      },
    });
  }
}
