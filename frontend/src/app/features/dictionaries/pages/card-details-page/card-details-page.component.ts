import { ChangeDetectionStrategy, Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { RouterLink } from '@angular/router';

import { BadgeComponent, BadgeVariant } from '../../../../shared/components/badge/badge.component';
import { BreadcrumbsComponent } from '../../../../shared/components/breadcrumbs/breadcrumbs.component';
import { InlineAlertComponent } from '../../../../shared/components/inline-alert/inline-alert.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { AppError } from '../../../../shared/models/api-error.model';
import { SAMPLE_CARD_DETAIL } from '../../dictionaries.sample-data';
import { CardType } from '../../models/card-type.enum';
import { CardDetail } from '../../models/card.model';
import { DictionaryApiService } from '../../services/dictionary-api.service';

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
    RouterLink,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressBarModule,
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

  readonly dictionaryId = input.required<string>();
  readonly cardId = input.required<string>();
  private readonly dictionaryIdAsNumber = computed(() => Number(this.dictionaryId()));
  private readonly cardIdAsNumber = computed(() => Number(this.cardId()));

  protected readonly card = signal<CardDetail | null>(SAMPLE_CARD_DETAIL);
  protected readonly loading = signal(false);
  protected readonly error = signal<AppError | null>(null);
  protected readonly usingSampleData = signal(true);

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
        this.usingSampleData.set(false);
        this.loading.set(false);
      },
      error: (error: AppError) => {
        this.error.set(error);
        this.loading.set(false);
      },
    });
  }
}
