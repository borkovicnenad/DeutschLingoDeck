import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { Router, RouterLink } from '@angular/router';

import { BadgeComponent, BadgeVariant } from '../../../../shared/components/badge/badge.component';
import { BreadcrumbsComponent } from '../../../../shared/components/breadcrumbs/breadcrumbs.component';
import { EmptyStateComponent } from '../../../../shared/components/empty-state/empty-state.component';
import { InlineAlertComponent } from '../../../../shared/components/inline-alert/inline-alert.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { ConfirmDialogService } from '../../../../shared/dialogs/confirm-dialog/confirm-dialog.service';
import { AppError } from '../../../../shared/models/api-error.model';
import { AiRecommendation } from '../../../ai-assistant/models/ai-assistant.model';
import { AiAssistantService } from '../../../ai-assistant/services/ai-assistant.service';
import { SAMPLE_CARDS, SAMPLE_DICTIONARY_DETAIL } from '../../dictionaries.sample-data';
import { CardType } from '../../models/card-type.enum';
import { CardFilter, CardFilterStatus, CardSummary } from '../../models/card.model';
import { DictionaryDetail } from '../../models/dictionary.model';
import { DictionaryApiService } from '../../services/dictionary-api.service';

const CARD_TYPES: CardType[] = ['WORD', 'NOUN', 'VERB', 'PHRASE', 'SENTENCE'];

const CARD_TYPE_BADGE_VARIANT: Record<CardType, BadgeVariant> = {
  NOUN: 'info',
  VERB: 'success',
  PHRASE: 'warning',
  SENTENCE: 'neutral',
  WORD: 'neutral',
};

@Component({
  selector: 'app-dictionary-details-page',
  imports: [
    DatePipe,
    FormsModule,
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatTableModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatSelectModule,
    PageHeaderComponent,
    BreadcrumbsComponent,
    BadgeComponent,
    EmptyStateComponent,
    InlineAlertComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './dictionary-details-page.component.html',
  styleUrl: './dictionary-details-page.component.css',
})
export class DictionaryDetailsPageComponent implements OnInit {
  private readonly dictionaryApi = inject(DictionaryApiService);
  private readonly confirmDialog = inject(ConfirmDialogService);
  private readonly aiAssistant = inject(AiAssistantService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly router = inject(Router);

  readonly dictionaryId = input.required<string>();
  private readonly dictionaryIdAsNumber = computed(() => Number(this.dictionaryId()));

  protected readonly dictionary = signal<DictionaryDetail | null>(SAMPLE_DICTIONARY_DETAIL);
  protected readonly loading = signal(false);
  protected readonly error = signal<AppError | null>(null);
  protected readonly usingSampleData = signal(true);
  protected readonly editing = signal(false);

  protected readonly cards = signal<CardSummary[]>(SAMPLE_CARDS);
  protected readonly cardsLoading = signal(false);
  protected readonly cardsPage = signal(0);
  protected readonly cardsTotal = signal(SAMPLE_CARDS.length);
  protected readonly displayedColumns = ['sourceText', 'primaryTranslation', 'cardType', 'actions'];

  protected readonly cardTypes = CARD_TYPES;
  protected readonly filterSearch = signal('');
  protected readonly filterStatus = signal<CardFilterStatus | ''>('');

  protected readonly addingCard = signal(false);
  protected readonly savingCard = signal(false);
  protected readonly cardForm = this.formBuilder.nonNullable.group({
    cardType: ['WORD' as CardType, [Validators.required]],
    article: [''],
    sourceText: ['', [Validators.required, Validators.maxLength(255)]],
    primaryTranslation: ['', [Validators.maxLength(255)]],
    example: ['', [Validators.maxLength(1000)]],
    notes: ['', [Validators.maxLength(1000)]],
    difficultyLevel: [null as number | null],
    tags: [''],
  });

  protected readonly recommendations = signal<AiRecommendation[]>([]);
  protected readonly recommendationsLoading = signal(true);
  protected readonly recommendationsError = signal<string | null>(null);

  protected readonly editForm = this.formBuilder.nonNullable.group({
    name: [SAMPLE_DICTIONARY_DETAIL.name, [Validators.required, Validators.maxLength(150)]],
    description: [SAMPLE_DICTIONARY_DETAIL.description ?? '', [Validators.maxLength(1000)]],
    sourceLanguage: [SAMPLE_DICTIONARY_DETAIL.sourceLanguage, [Validators.required]],
    targetLanguage: [SAMPLE_DICTIONARY_DETAIL.targetLanguage, [Validators.required]],
  });

  ngOnInit(): void {
    this.loadDictionary();
    this.loadCards(0);
    this.loadRecommendations();
  }

  protected cardTypeBadgeVariant(cardType: CardType): BadgeVariant {
    return CARD_TYPE_BADGE_VARIANT[cardType];
  }

  protected loadRecommendations(): void {
    this.recommendationsLoading.set(true);
    this.recommendationsError.set(null);

    this.aiAssistant.getRecommendations(this.dictionaryIdAsNumber()).subscribe({
      next: (recommendations) => {
        this.recommendations.set(recommendations);
        this.recommendationsLoading.set(false);
      },
      error: (error: Error) => {
        this.recommendationsError.set(error.message);
        this.recommendationsLoading.set(false);
      },
    });
  }

  protected loadDictionary(): void {
    this.loading.set(true);
    this.error.set(null);

    this.dictionaryApi.getById(this.dictionaryIdAsNumber()).subscribe({
      next: (dictionary) => {
        this.dictionary.set(dictionary);
        this.editForm.patchValue(dictionary);
        this.usingSampleData.set(false);
        this.loading.set(false);
      },
      error: (error: AppError) => {
        this.error.set(error);
        this.loading.set(false);
      },
    });
  }

  private loadCards(page: number): void {
    this.cardsLoading.set(true);

    const filter: CardFilter = {
      search: this.filterSearch() || undefined,
      status: this.filterStatus() || undefined,
    };

    this.dictionaryApi.listCards(this.dictionaryIdAsNumber(), page, 20, filter).subscribe({
      next: (response) => {
        this.cards.set(response.content);
        this.cardsPage.set(response.page);
        this.cardsTotal.set(response.totalElements);
        this.cardsLoading.set(false);
      },
      error: () => this.cardsLoading.set(false),
    });
  }

  protected onCardsPage(event: PageEvent): void {
    this.loadCards(event.pageIndex);
  }

  protected applyFilters(): void {
    this.loadCards(0);
  }

  protected startAddingCard(): void {
    this.cardForm.reset({ cardType: 'WORD', difficultyLevel: null });
    this.addingCard.set(true);
  }

  protected cancelAddingCard(): void {
    this.addingCard.set(false);
  }

  protected saveNewCard(): void {
    if (this.cardForm.invalid || this.savingCard()) {
      this.cardForm.markAllAsTouched();
      return;
    }

    const values = this.cardForm.getRawValue();
    this.savingCard.set(true);

    this.dictionaryApi
      .createCard(this.dictionaryIdAsNumber(), {
        cardType: values.cardType,
        article: values.article || undefined,
        sourceText: values.sourceText,
        primaryTranslation: values.primaryTranslation || undefined,
        acceptedAnswers: values.primaryTranslation ? [values.primaryTranslation] : undefined,
        example: values.example || undefined,
        notes: values.notes || undefined,
        difficultyLevel: values.difficultyLevel ?? undefined,
        tags: this.splitTags(values.tags),
      })
      .subscribe({
        next: () => {
          this.savingCard.set(false);
          this.addingCard.set(false);
          this.loadCards(this.cardsPage());
          this.loadDictionary();
        },
        error: () => this.savingCard.set(false),
      });
  }

  protected deleteCard(card: CardSummary): void {
    this.confirmDialog
      .confirm({
        title: 'Delete card',
        message: `Remove "${card.sourceText}" from this dictionary? This cannot be undone.`,
        confirmLabel: 'Delete',
      })
      .subscribe((confirmed) => {
        if (!confirmed) {
          return;
        }
        this.dictionaryApi.deleteCard(this.dictionaryIdAsNumber(), card.id).subscribe(() => {
          this.loadCards(this.cardsPage());
          this.loadDictionary();
        });
      });
  }

  private splitTags(raw: string): string[] | undefined {
    const tags = raw
      .split(',')
      .map((tag) => tag.trim())
      .filter((tag) => tag.length > 0);
    return tags.length > 0 ? tags : undefined;
  }

  protected startEditing(): void {
    this.editing.set(true);
  }

  protected cancelEditing(): void {
    const dictionary = this.dictionary();
    if (dictionary) {
      this.editForm.patchValue(dictionary);
    }
    this.editing.set(false);
  }

  protected saveEditing(): void {
    if (this.editForm.invalid) {
      this.editForm.markAllAsTouched();
      return;
    }

    this.dictionaryApi
      .update(this.dictionaryIdAsNumber(), this.editForm.getRawValue())
      .subscribe((dictionary) => {
        this.dictionary.set(dictionary);
        this.editing.set(false);
      });
  }

  protected startGame(): void {
    this.router.navigate(['/game/start'], {
      queryParams: { dictionaryId: this.dictionaryIdAsNumber() },
    });
  }

  protected deleteDictionary(): void {
    const dictionary = this.dictionary();
    if (!dictionary) {
      return;
    }

    this.confirmDialog
      .confirm({
        title: 'Delete dictionary',
        message: `"${dictionary.name}" will no longer be available for future learning sessions. Your historical statistics and completed games remain preserved.`,
        confirmLabel: 'Delete',
      })
      .subscribe((confirmed) => {
        if (!confirmed) {
          return;
        }

        this.dictionaryApi.delete(this.dictionaryIdAsNumber()).subscribe(() => {
          this.router.navigateByUrl('/dictionaries');
        });
      });
  }
}
