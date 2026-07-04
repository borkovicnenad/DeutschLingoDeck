import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, of, switchMap } from 'rxjs';

import { API_BASE_URL } from '../../../core/config/api-base-url.token';
import { PageResponse } from '../../../shared/models/page-response.model';
import { GameStatus } from '../models/game-status.enum';
import {
  AnswerValidationResponse,
  CreateGameRequest,
  Game,
  GameSummary,
  SubmitAnswerRequest,
} from '../models/game.model';

export interface GameHistoryFilter {
  status?: GameStatus;
  dictionaryId?: number;
  page?: number;
  size?: number;
}

/** All backend communication for the Games feature. */
@Injectable({ providedIn: 'root' })
export class GameApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  start(request: CreateGameRequest): Observable<Game> {
    return this.http.post<Game>(`${this.baseUrl}/games`, request);
  }

  getById(gameId: number): Observable<Game> {
    return this.http.get<Game>(`${this.baseUrl}/games/${gameId}`);
  }

  submitAnswer(gameId: number, request: SubmitAnswerRequest): Observable<AnswerValidationResponse> {
    return this.http.post<AnswerValidationResponse>(
      `${this.baseUrl}/games/${gameId}/answers`,
      request,
    );
  }

  finish(gameId: number): Observable<GameSummary> {
    return this.http.post<GameSummary>(`${this.baseUrl}/games/${gameId}/finish`, {});
  }

  abandon(gameId: number): Observable<GameSummary> {
    return this.http.post<GameSummary>(`${this.baseUrl}/games/${gameId}/abandon`, {});
  }

  getSummary(gameId: number): Observable<GameSummary> {
    return this.http.get<GameSummary>(`${this.baseUrl}/games/${gameId}/summary`);
  }

  list(filter: GameHistoryFilter = {}): Observable<PageResponse<GameSummary>> {
    let params = new HttpParams()
      .set('page', filter.page ?? 0)
      .set('size', filter.size ?? 20);

    if (filter.status) {
      params = params.set('status', filter.status);
    }
    if (filter.dictionaryId) {
      params = params.set('dictionaryId', filter.dictionaryId);
    }

    return this.http.get<PageResponse<GameSummary>>(`${this.baseUrl}/games`, { params });
  }

  /** Resolves the user's current unfinished game (if any), used by UC-035 "Resume Active Game". */
  findActiveGame(): Observable<Game | null> {
    return this.list({ status: 'IN_PROGRESS', size: 1 }).pipe(
      switchMap((response) => {
        const [activeGame] = response.content;
        return activeGame ? this.getById(activeGame.gameId) : of(null);
      }),
    );
  }
}
