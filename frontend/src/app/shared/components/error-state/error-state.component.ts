import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-error-state',
  imports: [MatIconModule, MatButtonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="error-state" role="alert">
      <mat-icon class="error-state__icon">error_outline</mat-icon>
      <h3 class="error-state__title">{{ title() }}</h3>
      <p class="error-state__message">{{ message() }}</p>
      @if (retryable()) {
        <button matButton="filled" type="button" (click)="retry.emit()">Try again</button>
      }
    </div>
  `,
  styles: `
    .error-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
      gap: 0.5rem;
      padding: 3rem 1rem;
    }

    .error-state__icon {
      font-size: 2.5rem;
      height: 2.5rem;
      width: 2.5rem;
      color: var(--mat-sys-error);
    }

    .error-state__title {
      margin: 0;
    }

    .error-state__message {
      margin: 0 0 0.5rem;
      max-width: 32rem;
      color: var(--mat-sys-on-surface-variant);
    }
  `,
})
export class ErrorStateComponent {
  readonly title = input('Something went wrong');
  readonly message = input('Please try again in a moment.');
  readonly retryable = input(true);
  readonly retry = output<void>();
}
