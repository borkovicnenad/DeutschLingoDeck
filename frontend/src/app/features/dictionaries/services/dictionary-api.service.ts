import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../../../core/config/api-base-url.token';
import { PageResponse } from '../../../shared/models/page-response.model';
import { CardDetail, CardSummary } from '../models/card.model';
import {
  DictionaryDetail,
  DictionaryImportRequest,
  DictionaryImportResponse,
  DictionarySummary,
  UpdateDictionaryRequest,
} from '../models/dictionary.model';

/** All backend communication for the Dictionaries feature. */
@Injectable({ providedIn: 'root' })
export class DictionaryApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  list(page = 0, size = 20): Observable<PageResponse<DictionarySummary>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<DictionarySummary>>(`${this.baseUrl}/dictionaries`, {
      params,
    });
  }

  import(request: DictionaryImportRequest): Observable<DictionaryImportResponse> {
    const formData = new FormData();
    formData.append('file', request.file);
    formData.append('name', request.name);
    if (request.description) {
      formData.append('description', request.description);
    }
    formData.append('sourceLanguage', request.sourceLanguage);
    formData.append('targetLanguage', request.targetLanguage);

    return this.http.post<DictionaryImportResponse>(
      `${this.baseUrl}/dictionaries/import`,
      formData,
    );
  }

  getById(dictionaryId: number): Observable<DictionaryDetail> {
    return this.http.get<DictionaryDetail>(`${this.baseUrl}/dictionaries/${dictionaryId}`);
  }

  update(
    dictionaryId: number,
    request: UpdateDictionaryRequest,
  ): Observable<DictionaryDetail> {
    return this.http.put<DictionaryDetail>(
      `${this.baseUrl}/dictionaries/${dictionaryId}`,
      request,
    );
  }

  delete(dictionaryId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/dictionaries/${dictionaryId}`);
  }

  listCards(
    dictionaryId: number,
    page = 0,
    size = 20,
  ): Observable<PageResponse<CardSummary>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<CardSummary>>(
      `${this.baseUrl}/dictionaries/${dictionaryId}/cards`,
      { params },
    );
  }

  getCard(dictionaryId: number, cardId: number): Observable<CardDetail> {
    return this.http.get<CardDetail>(
      `${this.baseUrl}/dictionaries/${dictionaryId}/cards/${cardId}`,
    );
  }
}
