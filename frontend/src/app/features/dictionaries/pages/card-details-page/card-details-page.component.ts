import { ChangeDetectionStrategy, Component, computed, inject, input, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { RouterLink } from '@angular/router';

import { InlineAlertComponent } from '../../../../shared/components/inline-alert/inline-alert.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { AppError } from '../../../../shared/models/api-error.model';
import { SAMPLE_CARD_DETAIL } from '../../dictionaries.sample-data';
import { CardDetail } from '../../models/card.model';
import { DictionaryApiService } from '../../services/dictionary-api.service';

@Component({
  selector: 'app-card-details-page',
  imports: [
    RouterLink,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressBarModule,
    PageHeaderComponent,
    InlineAlertComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './card-details-page.component.html',
  styleUrl: './card-details-page.component.css',
})
export class CardDetailsPageComponent {
  private readonly dictionaryApi = inject(DictionaryApiService);

  readonly dictionaryId = input.required<string>();
  readonly cardId = input.required<string>();
  private readonly dictionaryIdAsNumber = computed(() => Number(this.dictionaryId()));
  private readonly cardIdAsNumber = computed(() => Number(this.cardId()));

  protected readonly card = signal<CardDetail | null>(SAMPLE_CARD_DETAIL);
  protected readonly loading = signal(false);
  protected readonly error = signal<AppError | null>(null);
  protected readonly usingSampleData = signal(true);

  constructor() {
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
