import { ChangeDetectionStrategy, Component, output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { RouterLink, RouterLinkActive } from '@angular/router';

import { NAVIGATION_ITEMS } from '../navigation/navigation.items';

@Component({
  selector: 'app-sidebar',
  imports: [MatListModule, MatIconModule, RouterLink, RouterLinkActive],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <mat-nav-list>
      @for (item of items; track item.route) {
        <a
          mat-list-item
          [routerLink]="item.route"
          routerLinkActive="sidebar__link--active"
          (click)="linkClicked.emit()"
        >
          <mat-icon matListItemIcon>{{ item.icon }}</mat-icon>
          <span matListItemTitle>{{ item.label }}</span>
        </a>
      }
    </mat-nav-list>
  `,
  styles: `
    .sidebar__link--active {
      background: var(--mat-sys-secondary-container);
      color: var(--mat-sys-on-secondary-container);
    }
  `,
})
export class SidebarComponent {
  protected readonly items = NAVIGATION_ITEMS;
  readonly linkClicked = output<void>();
}
