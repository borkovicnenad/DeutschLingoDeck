import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

export type BadgeVariant = 'neutral' | 'success' | 'warning' | 'danger' | 'info';

@Component({
  selector: 'app-badge',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<span class="badge" [class]="variantClass()"><ng-content /></span>`,
  styles: `
    .badge {
      display: inline-flex;
      align-items: center;
      padding: 0.125rem 0.625rem;
      border-radius: 999px;
      font-size: 0.75rem;
      font-weight: 500;
      line-height: 1.5rem;
    }

    .badge--neutral {
      background: var(--mat-sys-surface-variant);
      color: var(--mat-sys-on-surface-variant);
    }

    .badge--success {
      background: #d3ead9;
      color: #1e4620;
    }

    .badge--warning {
      background: #fbe7c6;
      color: #6b4a00;
    }

    .badge--danger {
      background: #f8d7da;
      color: #7a1f27;
    }

    .badge--info {
      background: #d6e4f0;
      color: #1c3d5a;
    }
  `,
})
export class BadgeComponent {
  readonly variant = input<BadgeVariant>('neutral');
  protected readonly variantClass = computed(() => `badge--${this.variant()}`);
}
