import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';

export interface BreadcrumbItem {
  label: string;
  link?: string[];
}

@Component({
  selector: 'app-breadcrumbs',
  imports: [RouterLink, MatIconModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <nav class="breadcrumbs" aria-label="Breadcrumb">
      @for (item of items(); track item.label; let last = $last) {
        @if (item.link && !last) {
          <a class="breadcrumbs__link" [routerLink]="item.link">{{ item.label }}</a>
        } @else {
          <span class="breadcrumbs__current">{{ item.label }}</span>
        }
        @if (!last) {
          <mat-icon class="breadcrumbs__separator">chevron_right</mat-icon>
        }
      }
    </nav>
  `,
  styles: `
    .breadcrumbs {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 0.125rem;
      margin-bottom: 0.75rem;
      font-size: 0.875rem;
      color: var(--mat-sys-on-surface-variant);
    }

    .breadcrumbs__link {
      color: var(--mat-sys-on-surface-variant);
      text-decoration: none;
    }

    .breadcrumbs__link:hover {
      color: var(--mat-sys-primary);
      text-decoration: underline;
    }

    .breadcrumbs__current {
      color: var(--mat-sys-on-surface);
      font-weight: 500;
    }

    .breadcrumbs__separator {
      font-size: 1rem;
      height: 1rem;
      width: 1rem;
      opacity: 0.6;
    }
  `,
})
export class BreadcrumbsComponent {
  readonly items = input.required<BreadcrumbItem[]>();
}
