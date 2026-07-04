import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatSidenavModule } from '@angular/material/sidenav';
import { RouterOutlet } from '@angular/router';
import { map } from 'rxjs';

import { FooterComponent } from '../footer/footer.component';
import { HeaderComponent } from '../header/header.component';
import { SidebarComponent } from '../sidebar/sidebar.component';

/** Common authenticated layout: header, collapsible sidebar, content area and footer. */
@Component({
  selector: 'app-shell',
  imports: [MatSidenavModule, RouterOutlet, HeaderComponent, SidebarComponent, FooterComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-header (menuToggle)="toggleSidenav()" />

    <mat-sidenav-container class="shell">
      <mat-sidenav
        #sidenav
        class="shell__sidenav"
        [mode]="isHandset() ? 'over' : 'side'"
        [opened]="isHandset() ? sidenavOpened() : true"
        (openedChange)="sidenavOpened.set($event)"
      >
        <app-sidebar (linkClicked)="isHandset() && sidenav.close()" />
      </mat-sidenav>

      <mat-sidenav-content class="shell__content">
        <main class="shell__main">
          <router-outlet />
        </main>
        <app-footer />
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: `
    .shell {
      height: calc(100dvh - 64px);
    }

    .shell__sidenav {
      width: 260px;
    }

    .shell__main {
      padding: 1.5rem;
      min-height: calc(100% - 3rem);
      box-sizing: border-box;
    }
  `,
})
export class AppShellComponent {
  private readonly breakpointObserver = inject(BreakpointObserver);

  protected readonly isHandset = toSignal(
    this.breakpointObserver
      .observe(Breakpoints.Handset)
      .pipe(map((result) => result.matches)),
    { initialValue: false },
  );

  protected readonly sidenavOpened = signal(false);

  protected toggleSidenav(): void {
    this.sidenavOpened.update((opened) => !opened);
  }
}
