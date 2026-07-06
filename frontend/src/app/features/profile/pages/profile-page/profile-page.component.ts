import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';

import { InlineAlertComponent } from '../../../../shared/components/inline-alert/inline-alert.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { AvatarComponent } from '../../../../shared/components/avatar/avatar.component';
import { passwordsMatchValidator } from '../../../../shared/utils/password-match.validator';
import { AppError } from '../../../../shared/models/api-error.model';
import { AuthService } from '../../../../core/auth/auth.service';
import {
  SAMPLE_ACHIEVEMENTS,
  SAMPLE_LEVEL_PROGRESS,
} from '../../../gamification/gamification.sample-data';
import { Achievement, LevelProgress } from '../../../gamification/models/gamification.model';
import { GamificationService } from '../../../gamification/services/gamification.service';
import { SAMPLE_PROFILE } from '../../profile.sample-data';
import { Profile } from '../../models/profile.model';
import { ProfileApiService } from '../../services/profile-api.service';

@Component({
  selector: 'app-profile-page',
  imports: [
    DatePipe,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatTooltipModule,
    PageHeaderComponent,
    InlineAlertComponent,
    AvatarComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './profile-page.component.html',
  styleUrl: './profile-page.component.css',
})
export class ProfilePageComponent {
  private readonly profileApi = inject(ProfileApiService);
  private readonly authService = inject(AuthService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly gamificationApi = inject(GamificationService);

  protected readonly profile = signal<Profile | null>(SAMPLE_PROFILE);
  protected readonly loading = signal(false);
  protected readonly error = signal<AppError | null>(null);
  protected readonly usingSampleData = signal(true);

  protected readonly levelProgress = signal<LevelProgress | null>(SAMPLE_LEVEL_PROGRESS);
  protected readonly achievements = signal<Achievement[]>(SAMPLE_ACHIEVEMENTS);

  protected readonly profileSaving = signal(false);
  protected readonly profileSaved = signal(false);
  protected readonly profileError = signal<string | null>(null);

  protected readonly passwordSaving = signal(false);
  protected readonly passwordSaved = signal(false);
  protected readonly passwordError = signal<string | null>(null);

  protected readonly profileForm = this.formBuilder.nonNullable.group({
    displayName: [
      SAMPLE_PROFILE.displayName,
      [Validators.required, Validators.minLength(2), Validators.maxLength(100)],
    ],
  });

  protected readonly passwordForm = this.formBuilder.nonNullable.group(
    {
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(12)]],
      confirmNewPassword: ['', [Validators.required]],
    },
    { validators: passwordsMatchValidator('newPassword', 'confirmNewPassword') },
  );

  constructor() {
    this.load();
    this.loadGamification();
  }

  protected xpPercentage(progress: LevelProgress): number {
    return Math.min(100, (progress.currentXp / progress.xpToNextLevel) * 100);
  }

  private loadGamification(): void {
    this.gamificationApi.getLevelProgress().subscribe((progress) => this.levelProgress.set(progress));
    this.gamificationApi.getAchievements().subscribe((achievements) => this.achievements.set(achievements));
  }

  protected load(): void {
    this.loading.set(true);
    this.error.set(null);

    this.profileApi.getProfile().subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.profileForm.patchValue({ displayName: profile.displayName });
        this.usingSampleData.set(false);
        this.loading.set(false);
      },
      error: (error: AppError) => {
        this.error.set(error);
        this.loading.set(false);
      },
    });
  }

  protected saveProfile(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    this.profileSaving.set(true);
    this.profileSaved.set(false);
    this.profileError.set(null);

    this.profileApi.updateProfile(this.profileForm.getRawValue()).subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.authService.updateDisplayName(profile.displayName);
        this.profileSaving.set(false);
        this.profileSaved.set(true);
      },
      error: (error: AppError) => {
        this.profileSaving.set(false);
        this.profileError.set(error.message ?? 'Could not update profile.');
      },
    });
  }

  protected changePassword(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    this.passwordSaving.set(true);
    this.passwordSaved.set(false);
    this.passwordError.set(null);

    const { currentPassword, newPassword } = this.passwordForm.getRawValue();

    this.profileApi.changePassword({ currentPassword, newPassword }).subscribe({
      next: () => {
        this.passwordSaving.set(false);
        this.passwordSaved.set(true);
        this.passwordForm.reset();
      },
      error: (error: AppError) => {
        this.passwordSaving.set(false);
        this.passwordError.set(error.message ?? 'Could not change password.');
      },
    });
  }
}
