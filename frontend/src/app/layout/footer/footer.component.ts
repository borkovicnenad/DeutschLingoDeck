import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-footer',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <footer class="footer">
      <p>DeutschLingoDeck &copy; {{ year }}</p>
    </footer>
  `,
  styles: `
    .footer {
      padding: 1rem;
      text-align: center;
      color: var(--mat-sys-on-surface-variant);
      font-size: 0.75rem;
    }
  `,
})
export class FooterComponent {
  protected readonly year = new Date().getFullYear();
}
