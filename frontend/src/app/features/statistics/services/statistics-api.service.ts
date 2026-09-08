import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../../../core/config/api-base-url.token';
import { PageResponse } from '../../../shared/models/page-response.model';
import {
  CardStatistics,
  DashboardStatistics,
  DictionaryStatistics,
  LearningHistoryEntry,
  MasteryBreakdown,
} from '../models/statistics.model';

export interface StatisticsFilter {
  from?: string;
  to?: string;
  dictionaryId?: number;
  gameMode?: string;
}

/** All backend communication for the Statistics feature. */
@Injectable({ providedIn: 'root' })
export class StatisticsApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  getDashboardStatistics(filter: StatisticsFilter = {}): Observable<DashboardStatistics> {
    return this.http.get<DashboardStatistics>(`${this.baseUrl}/statistics`, {
      params: this.toHttpParams(filter),
    });
  }

  getCardStatistics(
    filter: StatisticsFilter = {},
    page = 0,
    size = 20,
  ): Observable<PageResponse<CardStatistics>> {
    const params = this.toHttpParams(filter).set('page', page).set('size', size);
    return this.http.get<PageResponse<CardStatistics>>(`${this.baseUrl}/statistics/cards`, {
      params,
    });
  }

  getDictionaryStatistics(): Observable<DictionaryStatistics[]> {
    return this.http.get<DictionaryStatistics[]>(`${this.baseUrl}/statistics/dictionaries`);
  }

  getMasteryBreakdown(dictionaryId?: number): Observable<MasteryBreakdown> {
    let params = new HttpParams();
    if (dictionaryId) {
      params = params.set('dictionaryId', dictionaryId);
    }
    return this.http.get<MasteryBreakdown>(`${this.baseUrl}/statistics/mastery`, { params });
  }

  getLearningHistory(
    filter: StatisticsFilter & { page?: number; size?: number } = {},
  ): Observable<PageResponse<LearningHistoryEntry>> {
    const params = this.toHttpParams(filter)
      .set('page', filter.page ?? 0)
      .set('size', filter.size ?? 20);
    return this.http.get<PageResponse<LearningHistoryEntry>>(`${this.baseUrl}/statistics/history`, {
      params,
    });
  }

  private toHttpParams(filter: StatisticsFilter): HttpParams {
    let params = new HttpParams();
    if (filter.from) {
      params = params.set('from', filter.from);
    }
    if (filter.to) {
      params = params.set('to', filter.to);
    }
    if (filter.dictionaryId) {
      params = params.set('dictionaryId', filter.dictionaryId);
    }
    if (filter.gameMode) {
      params = params.set('gameMode', filter.gameMode);
    }
    return params;
  }
}
