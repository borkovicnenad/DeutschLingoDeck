import { ChangeDetectionStrategy, Component, inject, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterLink } from '@angular/router';

import { AuthService } from '../../core/auth/auth.service';
import { AvatarComponent } from '../../shared/components/avatar/avatar.component';

@Component({
  selector: 'app-header',
  imports: [MatToolbarModule, MatButtonModule, MatIconModule, MatMenuModule, RouterLink, AvatarComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <mat-toolbar color="primary" class="header">
      <button
        matIconButton
        type="button"
        class="header__menu-toggle"
        aria-label="Toggle navigation"
        (click)="menuToggle.emit()"
      >
        <mat-icon>menu</mat-icon>
      </button>

      <a routerLink="/dashboard" class="header__brand">DeutschLingoDeck</a>

      <span class="header__spacer"></span>

      @if (authService.currentUser(); as user) {
        <button matButton [matMenuTriggerFor]="userMenu" type="button" class="header__user">
          <app-avatar [name]="user.displayName" [size]="28" />
          {{ user.displayName }}
        </button>
        <mat-menu #userMenu="matMenu">
          <a mat-menu-item routerLink="/profile">
            <mat-icon>settings</mat-icon>
            <span>Profile</span>
          </a>
          <button mat-menu-item type="button" (click)="authService.logout()">
            <mat-icon>logout</mat-icon>
            <span>Logout</span>
          </button>
        </mat-menu>
      }
    </mat-toolbar>
  `,
  styles: `
    .header {
      position: sticky;
      top: 0;
      z-index: 10;
    }

    .header__brand {
      color: inherit;
      text-decoration: none;
      font-weight: 600;
      margin-left: 0.5rem;
    }

    .header__spacer {
      flex: 1 1 auto;
    }

    .header__user {
      display: flex;
      align-items: center;
      gap: 0.375rem;
    }

    @media (min-width: 960px) {
      .header__menu-toggle {
        display: none;
      }
    }
  `,
})
export class HeaderComponent {
  protected readonly authService = inject(AuthService);
  readonly menuToggle = output<void>();
}
