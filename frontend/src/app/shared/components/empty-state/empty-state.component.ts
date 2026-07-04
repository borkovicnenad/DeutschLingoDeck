import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-empty-state',
  imports: [MatIconModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="empty-state">
      <mat-icon class="empty-state__icon">{{ icon() }}</mat-icon>
      <h3 class="empty-state__title">{{ title() }}</h3>
      @if (message()) {
        <p class="empty-state__message">{{ message() }}</p>
      }
      <ng-content select="[actions]" />
    </div>
  `,
  styles: `
    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
      gap: 0.5rem;
      padding: 3rem 1rem;
      color: var(--mat-sys-on-surface-variant);
    }

    .empty-state__icon {
      font-size: 2.5rem;
      height: 2.5rem;
      width: 2.5rem;
      opacity: 0.6;
    }

    .empty-state__title {
      margin: 0;
      color: var(--mat-sys-on-surface);
    }

    .empty-state__message {
      margin: 0;
      max-width: 32rem;
    }
  `,
})
export class EmptyStateComponent {
  readonly icon = input('inbox');
  readonly title = input('Nothing here yet');
  readonly message = input<string | undefined>(undefined);
}
