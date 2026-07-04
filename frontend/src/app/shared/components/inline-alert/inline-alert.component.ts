import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export type InlineAlertTone = 'info' | 'warning';

/**
 * Compact, non-blocking banner shown above already-rendered content — e.g. to
 * note that a page is showing sample data because the live request failed,
 * without hiding that content behind a full-page error state.
 */
@Component({
  selector: 'app-inline-alert',
  imports: [MatIconModule, MatButtonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="inline-alert" [class.inline-alert--warning]="tone() === 'warning'" role="status">
      <mat-icon>{{ tone() === 'warning' ? 'cloud_off' : 'info' }}</mat-icon>
      <span class="inline-alert__message">{{ message() }}</span>
      @if (retryable()) {
        <button matButton type="button" (click)="retry.emit()">Retry</button>
      }
    </div>
  `,
  styles: `
    .inline-alert {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.5rem 0.875rem;
      border-radius: 0.5rem;
      background: var(--mat-sys-surface-variant);
      color: var(--mat-sys-on-surface-variant);
      font-size: 0.875rem;
      margin-bottom: 1rem;
    }

    .inline-alert--warning {
      background: #fbe7c6;
      color: #6b4a00;
    }

    .inline-alert__message {
      flex: 1;
    }
  `,
})
export class InlineAlertComponent {
  readonly message = input.required<string>();
  readonly tone = input<InlineAlertTone>('info');
  readonly retryable = input(false);
  readonly retry = output<void>();
}
