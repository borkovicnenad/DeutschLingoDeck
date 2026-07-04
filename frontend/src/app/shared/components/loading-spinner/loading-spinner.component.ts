import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-loading-spinner',
  imports: [MatProgressSpinnerModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="loading-spinner" role="status" [attr.aria-label]="label()">
      <mat-spinner [diameter]="diameter()" />
      @if (label()) {
        <p class="loading-spinner__label">{{ label() }}</p>
      }
    </div>
  `,
  styles: `
    .loading-spinner {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 1rem;
      padding: 2rem;
    }

    .loading-spinner__label {
      color: var(--mat-sys-on-surface-variant);
    }
  `,
})
export class LoadingSpinnerComponent {
  readonly diameter = input(40);
  readonly label = input('Loading...');
}
