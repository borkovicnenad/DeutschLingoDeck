import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router, RouterLink } from '@angular/router';

import { EmptyStateComponent } from '../../../../shared/components/empty-state/empty-state.component';
import { InlineAlertComponent } from '../../../../shared/components/inline-alert/inline-alert.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { ConfirmDialogService } from '../../../../shared/dialogs/confirm-dialog/confirm-dialog.service';
import { DictionaryApiService } from '../../services/dictionary-api.service';
import { DictionaryListStateService } from '../../state/dictionary-list-state.service';

@Component({
  selector: 'app-dictionary-list-page',
  imports: [
    DatePipe,
    RouterLink,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatMenuModule,
    MatTableModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatSortModule,
    MatTooltipModule,
    PageHeaderComponent,
    EmptyStateComponent,
    InlineAlertComponent,
  ],
  providers: [DictionaryListStateService],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './dictionary-list-page.component.html',
  styleUrl: './dictionary-list-page.component.css',
})
export class DictionaryListPageComponent implements OnInit {
  protected readonly state = inject(DictionaryListStateService);
  private readonly dictionaryApi = inject(DictionaryApiService);
  private readonly confirmDialog = inject(ConfirmDialogService);
  private readonly router = inject(Router);

  protected readonly displayedColumns = [
    'name',
    'languages',
    'cardCount',
    'createdAt',
    'actions',
  ];

  ngOnInit(): void {
    this.state.load();
  }

  protected onSearch(event: Event): void {
    this.state.setSearchTerm((event.target as HTMLInputElement).value);
  }

  protected onSort(sort: Sort): void {
    this.state.setSort(sort);
  }

  protected onPage(event: PageEvent): void {
    this.state.load(event.pageIndex, event.pageSize);
  }

  protected startGame(dictionaryId: number): void {
    this.router.navigate(['/game/start'], { queryParams: { dictionaryId } });
  }

  protected deleteDictionary(dictionaryId: number, name: string): void {
    this.confirmDialog
      .confirm({
        title: 'Delete dictionary',
        message: `"${name}" will no longer be available for future learning sessions. Your historical statistics and completed games remain preserved.`,
        confirmLabel: 'Delete',
      })
      .subscribe((confirmed) => {
        if (!confirmed) {
          return;
        }

        this.dictionaryApi.delete(dictionaryId).subscribe(() => {
          this.state.removeLocally(dictionaryId);
        });
      });
  }
}
