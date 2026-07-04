import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-access-denied-page',
  imports: [RouterLink, MatButtonModule, MatIconModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="error-page">
      <mat-icon class="error-page__icon">block</mat-icon>
      <h1>Access denied</h1>
      <p>You do not have permission to view this resource.</p>
      <a matButton="filled" routerLink="/dashboard">Return to Dashboard</a>
    </div>
  `,
  styles: `
    .error-page {
      min-height: 100dvh;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      text-align: center;
      gap: 0.5rem;
      padding: 1rem;
    }

    .error-page__icon {
      font-size: 3rem;
      height: 3rem;
      width: 3rem;
      opacity: 0.6;
      margin-bottom: 0.5rem;
    }
  `,
})
export class AccessDeniedPageComponent {}
