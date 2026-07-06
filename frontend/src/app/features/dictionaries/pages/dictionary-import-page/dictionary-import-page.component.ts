import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin, map, switchMap } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { RouterLink } from '@angular/router';

import { BadgeComponent } from '../../../../shared/components/badge/badge.component';
import { BreadcrumbsComponent } from '../../../../shared/components/breadcrumbs/breadcrumbs.component';
import { ErrorStateComponent } from '../../../../shared/components/error-state/error-state.component';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { AppError } from '../../../../shared/models/api-error.model';
import { AiGeneratedCard } from '../../../ai-assistant/models/ai-assistant.model';
import { AiAssistantService } from '../../../ai-assistant/services/ai-assistant.service';
import { DictionaryImportResponse } from '../../models/dictionary.model';
import { DictionaryApiService } from '../../services/dictionary-api.service';

@Component({
  selector: 'app-dictionary-import-page',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    MatSelectModule,
    MatTableModule,
    MatTabsModule,
    PageHeaderComponent,
    BreadcrumbsComponent,
    BadgeComponent,
    LoadingSpinnerComponent,
    ErrorStateComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './dictionary-import-page.component.html',
  styleUrl: './dictionary-import-page.component.css',
})
export class DictionaryImportPageComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly dictionaryApi = inject(DictionaryApiService);
  private readonly aiAssistant = inject(AiAssistantService);

  protected readonly selectedFile = signal<File | null>(null);
  protected readonly uploading = signal(false);
  protected readonly importResult = signal<DictionaryImportResponse | null>(null);
  protected readonly submitError = signal<AppError | null>(null);

  protected readonly form = this.formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(150)]],
    description: ['', [Validators.maxLength(1000)]],
    sourceLanguage: ['hr', [Validators.required]],
    targetLanguage: ['de', [Validators.required]],
  });

  protected readonly aiGeneratedColumns = ['sourceText', 'primaryTranslation', 'cardType', 'estimatedDifficulty', 'confidence'];
  protected readonly aiGenerating = signal(false);
  protected readonly aiGeneratedCards = signal<AiGeneratedCard[] | null>(null);
  protected readonly aiError = signal<string | null>(null);
  protected readonly aiAdded = signal(false);
  protected readonly aiAdding = signal(false);
  protected readonly aiAddError = signal<string | null>(null);
  protected readonly aiCreatedDictionaryId = signal<number | null>(null);

  protected readonly aiForm = this.formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(150)]],
    topic: ['', [Validators.required, Validators.maxLength(100)]],
    level: ['A2', [Validators.required]],
  });

  protected generateWithAi(): void {
    if (this.aiForm.invalid || this.aiGenerating()) {
      this.aiForm.markAllAsTouched();
      return;
    }

    const { topic, level } = this.aiForm.getRawValue();
    this.aiGenerating.set(true);
    this.aiError.set(null);
    this.aiGeneratedCards.set(null);

    this.aiAssistant.generateVocabulary(topic, level).subscribe({
      next: (cards) => {
        this.aiGenerating.set(false);
        this.aiGeneratedCards.set(cards);
      },
      error: (error: Error) => {
        this.aiGenerating.set(false);
        this.aiError.set(error.message);
      },
    });
  }

  protected addGeneratedToDictionary(): void {
    const cards = this.aiGeneratedCards();
    if (!cards || cards.length === 0 || this.aiAdding()) {
      return;
    }

    const { name } = this.aiForm.getRawValue();
    const { sourceLanguage, targetLanguage } = this.form.getRawValue();

    this.aiAdding.set(true);
    this.aiAddError.set(null);

    this.dictionaryApi
      .create({ name, sourceLanguage, targetLanguage })
      .pipe(
        switchMap((dictionary) =>
          forkJoin(
            cards.map((card) =>
              this.dictionaryApi.createCard(dictionary.id, {
                cardType: card.cardType,
                sourceText: card.sourceText,
                primaryTranslation: card.primaryTranslation,
                acceptedAnswers: [card.primaryTranslation],
                example: card.exampleSentence,
                difficultyLevel: card.estimatedDifficulty,
              }),
            ),
          ).pipe(map(() => dictionary)),
        ),
      )
      .subscribe({
        next: (dictionary) => {
          this.aiAdding.set(false);
          this.aiAdded.set(true);
          this.aiCreatedDictionaryId.set(dictionary.id);
        },
        error: (error: AppError) => {
          this.aiAdding.set(false);
          this.aiAddError.set(error.message);
        },
      });
  }

  protected resetAiGeneration(): void {
    this.aiGeneratedCards.set(null);
    this.aiError.set(null);
    this.aiAdded.set(false);
    this.aiAddError.set(null);
    this.aiCreatedDictionaryId.set(null);
    this.aiForm.reset({ level: 'A2' });
  }

  protected onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile.set(input.files?.[0] ?? null);
  }

  protected submit(): void {
    const file = this.selectedFile();
    if (this.form.invalid || !file || this.uploading()) {
      this.form.markAllAsTouched();
      return;
    }

    this.uploading.set(true);
    this.submitError.set(null);
    this.importResult.set(null);

    this.dictionaryApi
      .import({ ...this.form.getRawValue(), file })
      .subscribe({
        next: (result) => {
          this.uploading.set(false);
          this.importResult.set(result);
        },
        error: (error: AppError) => {
          this.uploading.set(false);
          this.submitError.set(error);
        },
      });
  }

  protected reset(): void {
    this.form.reset({ sourceLanguage: 'hr', targetLanguage: 'de' });
    this.selectedFile.set(null);
    this.importResult.set(null);
    this.submitError.set(null);
  }
}
