import { Injectable } from '@angular/core';
import { Observable, delay, of } from 'rxjs';

import {
  SAMPLE_ACHIEVEMENTS,
  SAMPLE_DAILY_GOAL,
  SAMPLE_LEVEL_PROGRESS,
  SAMPLE_WEEKLY_ACTIVITY,
} from '../gamification.sample-data';
import { Achievement, DailyGoal, LevelProgress, WeeklyActivityPoint } from '../models/gamification.model';

/**
 * Frontend-only gamification layer. There is no backend for XP, levels,
 * achievements or goals, so this service simulates network latency around
 * static demo data instead of calling HttpClient, matching the shape callers
 * already expect from the real *-api.service.ts services.
 */
@Injectable({ providedIn: 'root' })
export class GamificationService {
  getLevelProgress(): Observable<LevelProgress> {
    return of(SAMPLE_LEVEL_PROGRESS).pipe(delay(300));
  }

  getDailyGoal(): Observable<DailyGoal> {
    return of(SAMPLE_DAILY_GOAL).pipe(delay(300));
  }

  getAchievements(): Observable<Achievement[]> {
    return of(SAMPLE_ACHIEVEMENTS).pipe(delay(400));
  }

  getWeeklyActivity(): Observable<WeeklyActivityPoint[]> {
    return of(SAMPLE_WEEKLY_ACTIVITY).pipe(delay(400));
  }
}
