import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { RouterLink } from '@angular/router';

import { ErrorStateComponent } from '../../../../shared/components/error-state/error-state.component';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { AppError } from '../../../../shared/models/api-error.model';
import { DictionaryImportResponse } from '../../models/dictionary.model';
import { DictionaryApiService } from '../../services/dictionary-api.service';

@Component({
  selector: 'app-dictionary-import-page',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    PageHeaderComponent,
    LoadingSpinnerComponent,
    ErrorStateComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './dictionary-import-page.component.html',
  styleUrl: './dictionary-import-page.component.css',
})
export class DictionaryImportPageComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly dictionaryApi = inject(DictionaryApiService);

  protected readonly selectedFile = signal<File | null>(null);
  protected readonly uploading = signal(false);
  protected readonly importResult = signal<DictionaryImportResponse | null>(null);
  protected readonly submitError = signal<AppError | null>(null);

  protected readonly form = this.formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(150)]],
    description: ['', [Validators.maxLength(1000)]],
    sourceLanguage: ['hr', [Validators.required]],
    targetLanguage: ['de', [Validators.required]],
  });

  protected onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile.set(input.files?.[0] ?? null);
  }

  protected submit(): void {
    const file = this.selectedFile();
    if (this.form.invalid || !file || this.uploading()) {
      this.form.markAllAsTouched();
      return;
    }

    this.uploading.set(true);
    this.submitError.set(null);
    this.importResult.set(null);

    this.dictionaryApi
      .import({ ...this.form.getRawValue(), file })
      .subscribe({
        next: (result) => {
          this.uploading.set(false);
          this.importResult.set(result);
        },
        error: (error: AppError) => {
          this.uploading.set(false);
          this.submitError.set(error);
        },
      });
  }

  protected reset(): void {
    this.form.reset({ sourceLanguage: 'hr', targetLanguage: 'de' });
    this.selectedFile.set(null);
    this.importResult.set(null);
    this.submitError.set(null);
  }
}
