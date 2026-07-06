import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../../../core/config/api-base-url.token';
import { Achievement, DailyGoal, LevelProgress, WeeklyActivityPoint } from '../models/gamification.model';

/**
 * XP, levels, achievements, daily goal and weekly activity, computed and persisted by the
 * backend from real game/answer history. One HTTP call per method, matching the shape this
 * service already exposed when it was a frontend-only demo layer.
 */
@Injectable({ providedIn: 'root' })
export class GamificationService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  getLevelProgress(): Observable<LevelProgress> {
    return this.http.get<LevelProgress>(`${this.baseUrl}/gamification/level-progress`);
  }

  getDailyGoal(): Observable<DailyGoal> {
    return this.http.get<DailyGoal>(`${this.baseUrl}/gamification/daily-goal`);
  }

  getAchievements(): Observable<Achievement[]> {
    return this.http.get<Achievement[]>(`${this.baseUrl}/gamification/achievements`);
  }

  getWeeklyActivity(): Observable<WeeklyActivityPoint[]> {
    return this.http.get<WeeklyActivityPoint[]>(`${this.baseUrl}/gamification/weekly-activity`);
  }
}
