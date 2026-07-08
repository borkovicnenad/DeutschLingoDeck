import { ChangeDetectionStrategy, Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { Router, RouterLink } from '@angular/router';

import { BadgeComponent, BadgeVariant } from '../../../../shared/components/badge/badge.component';
import { BreadcrumbsComponent } from '../../../../shared/components/breadcrumbs/breadcrumbs.component';
import { InlineAlertComponent } from '../../../../shared/components/inline-alert/inline-alert.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { ConfirmDialogService } from '../../../../shared/dialogs/confirm-dialog/confirm-dialog.service';
import { AppError } from '../../../../shared/models/api-error.model';
import { CardType } from '../../models/card-type.enum';
import { CardDetail } from '../../models/card.model';
import { DictionaryApiService } from '../../services/dictionary-api.service';

const CARD_TYPES: CardType[] = ['WORD', 'NOUN', 'VERB', 'PHRASE', 'SENTENCE'];

const CARD_TYPE_BADGE_VARIANT: Record<CardType, BadgeVariant> = {
  NOUN: 'info',
  VERB: 'success',
  PHRASE: 'warning',
  SENTENCE: 'neutral',
  WORD: 'neutral',
};

function difficultyBadgeVariant(level: number): BadgeVariant {
  if (level <= 2) {
    return 'success';
  }
  if (level === 3) {
    return 'warning';
  }
  return 'danger';
}

@Component({
  selector: 'app-card-details-page',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressBarModule,
    MatSelectModule,
    PageHeaderComponent,
    BreadcrumbsComponent,
    BadgeComponent,
    InlineAlertComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './card-details-page.component.html',
  styleUrl: './card-details-page.component.css',
})
export class CardDetailsPageComponent implements OnInit {
  private readonly dictionaryApi = inject(DictionaryApiService);
  private readonly confirmDialog = inject(ConfirmDialogService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly router = inject(Router);

  readonly dictionaryId = input.required<string>();
  readonly cardId = input.required<string>();
  private readonly dictionaryIdAsNumber = computed(() => Number(this.dictionaryId()));
  private readonly cardIdAsNumber = computed(() => Number(this.cardId()));

  protected readonly card = signal<CardDetail | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal<AppError | null>(null);
  protected readonly editing = signal(false);
  protected readonly saving = signal(false);

  protected readonly cardTypes = CARD_TYPES;
  protected readonly editForm = this.formBuilder.nonNullable.group({
    cardType: ['WORD' as CardType, [Validators.required]],
    article: [''],
    sourceText: ['', [Validators.required, Validators.maxLength(255)]],
    primaryTranslation: ['', [Validators.maxLength(255)]],
    example: ['', [Validators.maxLength(1000)]],
    grammarInfo: ['', [Validators.maxLength(1000)]],
    difficultyLevel: [null as number | null],
    tags: [''],
  });

  protected cardTypeBadgeVariant(cardType: CardType): BadgeVariant {
    return CARD_TYPE_BADGE_VARIANT[cardType];
  }

  protected difficultyBadgeVariant(level: number): BadgeVariant {
    return difficultyBadgeVariant(level);
  }

  ngOnInit(): void {
    this.load();
  }

  protected load(): void {
    this.loading.set(true);
    this.error.set(null);

    this.dictionaryApi.getCard(this.dictionaryIdAsNumber(), this.cardIdAsNumber()).subscribe({
      next: (card) => {
        this.card.set(card);
        this.loading.set(false);
      },
      error: (error: AppError) => {
        this.error.set(error);
        this.loading.set(false);
      },
    });
  }

  protected startEditing(): void {
    const card = this.card();
    if (!card) {
      return;
    }
    this.editForm.reset({
      cardType: card.cardType,
      article: card.article ?? '',
      sourceText: card.sourceText,
      primaryTranslation: card.primaryTranslation ?? '',
      example: card.example ?? '',
      grammarInfo: card.grammarInfo ?? '',
      difficultyLevel: card.difficultyLevel ?? null,
      tags: (card.tags ?? []).join(', '),
    });
    this.editing.set(true);
  }

  protected cancelEditing(): void {
    this.editing.set(false);
  }

  protected saveEditing(): void {
    if (this.editForm.invalid || this.saving()) {
      this.editForm.markAllAsTouched();
      return;
    }

    const values = this.editForm.getRawValue();
    this.saving.set(true);

    this.dictionaryApi
      .updateCard(this.dictionaryIdAsNumber(), this.cardIdAsNumber(), {
        cardType: values.cardType,
        article: values.article || undefined,
        sourceText: values.sourceText,
        primaryTranslation: values.primaryTranslation || undefined,
        acceptedAnswers: values.primaryTranslation ? [values.primaryTranslation] : undefined,
        example: values.example || undefined,
        grammarInfo: values.grammarInfo || undefined,
        difficultyLevel: values.difficultyLevel ?? undefined,
        tags: this.splitTags(values.tags),
      })
      .subscribe({
        next: (card) => {
          this.card.set(card);
          this.saving.set(false);
          this.editing.set(false);
        },
        error: () => this.saving.set(false),
      });
  }

  protected deleteCard(): void {
    const card = this.card();
    if (!card) {
      return;
    }

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
        this.dictionaryApi.deleteCard(this.dictionaryIdAsNumber(), this.cardIdAsNumber()).subscribe(() => {
          this.router.navigate(['/dictionaries', this.dictionaryId()]);
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
}
