import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-stat-card',
  imports: [MatCardModule, MatIconModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <mat-card class="stat-card" appearance="outlined">
      <mat-card-content class="stat-card__content">
        @if (icon()) {
          <mat-icon class="stat-card__icon">{{ icon() }}</mat-icon>
        }
        <div>
          <p class="stat-card__value">{{ value() }}</p>
          <p class="stat-card__label">{{ label() }}</p>
        </div>
      </mat-card-content>
    </mat-card>
  `,
  styles: `
    .stat-card__content {
      display: flex;
      align-items: center;
      gap: 1rem;
    }

    .stat-card__icon {
      color: var(--mat-sys-primary);
    }

    .stat-card__value {
      margin: 0;
      font-size: 1.5rem;
      font-weight: 600;
    }

    .stat-card__label {
      margin: 0;
      color: var(--mat-sys-on-surface-variant);
      font-size: 0.875rem;
    }
  `,
})
export class StatCardComponent {
  readonly label = input.required<string>();
  readonly value = input.required<string | number>();
  readonly icon = input<string | undefined>(undefined);
}
