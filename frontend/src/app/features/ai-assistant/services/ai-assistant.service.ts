import { Injectable } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';

import { AiGeneratedCard, AiRecommendation } from '../models/ai-assistant.model';

/**
 * TODO: no backend AI service exists yet (no endpoint under
 * /api/v1/ai-assistant or similar). Wire these methods up to real HTTP calls
 * once that service is built.
 */
@Injectable({ providedIn: 'root' })
export class AiAssistantService {
  generateVocabulary(_topic: string, _level: string): Observable<AiGeneratedCard[]> {
    return throwError(() => new Error('AI vocabulary generation is not available yet.'));
  }

  getRecommendations(_dictionaryId: number): Observable<AiRecommendation[]> {
    return of([]);
  }
}
