import { Injectable } from '@angular/core';
import { Observable, delay, of, throwError } from 'rxjs';

import { SAMPLE_CARDS } from '../../dictionaries/dictionaries.sample-data';
import { AiGeneratedCard, AiRecommendation } from '../models/ai-assistant.model';

const TOPIC_POOLS: Record<string, AiGeneratedCard[]> = {
  travel: [
    { sourceText: 'putovnica', primaryTranslation: 'der Reisepass', cardType: 'NOUN', exampleSentence: 'Ich brauche meinen Reisepass am Flughafen.', estimatedDifficulty: 2, confidence: 0.94 },
    { sourceText: 'karta za vlak', primaryTranslation: 'die Zugfahrkarte', cardType: 'NOUN', exampleSentence: 'Wo kann ich eine Zugfahrkarte kaufen?', estimatedDifficulty: 3, confidence: 0.88 },
    { sourceText: 'rezervirati', primaryTranslation: 'reservieren', cardType: 'VERB', exampleSentence: 'Ich möchte ein Zimmer reservieren.', estimatedDifficulty: 2, confidence: 0.91 },
    { sourceText: 'gdje je...?', primaryTranslation: 'Wo ist...?', cardType: 'PHRASE', exampleSentence: 'Wo ist der Bahnhof?', estimatedDifficulty: 1, confidence: 0.97 },
    { sourceText: 'zrakoplov', primaryTranslation: 'das Flugzeug', cardType: 'NOUN', exampleSentence: 'Das Flugzeug landet um 10 Uhr.', estimatedDifficulty: 2, confidence: 0.9 },
    { sourceText: 'prtljaga', primaryTranslation: 'das Gepäck', cardType: 'NOUN', exampleSentence: 'Mein Gepäck ist noch nicht angekommen.', estimatedDifficulty: 3, confidence: 0.85 },
  ],
  business: [
    { sourceText: 'sastanak', primaryTranslation: 'die Besprechung', cardType: 'NOUN', exampleSentence: 'Die Besprechung beginnt um 9 Uhr.', estimatedDifficulty: 3, confidence: 0.92 },
    { sourceText: 'pregovarati', primaryTranslation: 'verhandeln', cardType: 'VERB', exampleSentence: 'Wir verhandeln über den neuen Vertrag.', estimatedDifficulty: 4, confidence: 0.83 },
    { sourceText: 'rok', primaryTranslation: 'die Frist', cardType: 'NOUN', exampleSentence: 'Die Frist läuft nächste Woche ab.', estimatedDifficulty: 3, confidence: 0.87 },
    { sourceText: 'ponuda', primaryTranslation: 'das Angebot', cardType: 'NOUN', exampleSentence: 'Das Angebot gilt bis Freitag.', estimatedDifficulty: 3, confidence: 0.89 },
    { sourceText: 'suradnja', primaryTranslation: 'die Zusammenarbeit', cardType: 'NOUN', exampleSentence: 'Die Zusammenarbeit war sehr erfolgreich.', estimatedDifficulty: 4, confidence: 0.8 },
    { sourceText: 'potpisati ugovor', primaryTranslation: 'den Vertrag unterschreiben', cardType: 'PHRASE', exampleSentence: 'Wir unterschreiben den Vertrag morgen.', estimatedDifficulty: 4, confidence: 0.82 },
  ],
  food: [
    { sourceText: 'kruh', primaryTranslation: 'das Brot', cardType: 'NOUN', exampleSentence: 'Ich kaufe frisches Brot.', estimatedDifficulty: 1, confidence: 0.96 },
    { sourceText: 'kuhati', primaryTranslation: 'kochen', cardType: 'VERB', exampleSentence: 'Meine Mutter kocht jeden Sonntag.', estimatedDifficulty: 2, confidence: 0.93 },
    { sourceText: 'restoran', primaryTranslation: 'das Restaurant', cardType: 'NOUN', exampleSentence: 'Wir essen heute im Restaurant.', estimatedDifficulty: 1, confidence: 0.95 },
    { sourceText: 'dobar tek', primaryTranslation: 'Guten Appetit', cardType: 'PHRASE', exampleSentence: 'Guten Appetit allen!', estimatedDifficulty: 1, confidence: 0.98 },
    { sourceText: 'povrće', primaryTranslation: 'das Gemüse', cardType: 'NOUN', exampleSentence: 'Ich esse gerne Gemüse.', estimatedDifficulty: 2, confidence: 0.91 },
    { sourceText: 'račun, molim', primaryTranslation: 'die Rechnung, bitte', cardType: 'PHRASE', exampleSentence: 'Die Rechnung, bitte!', estimatedDifficulty: 1, confidence: 0.94 },
  ],
  family: [
    { sourceText: 'obitelj', primaryTranslation: 'die Familie', cardType: 'NOUN', exampleSentence: 'Meine Familie ist sehr groß.', estimatedDifficulty: 1, confidence: 0.97 },
    { sourceText: 'brat', primaryTranslation: 'der Bruder', cardType: 'NOUN', exampleSentence: 'Mein Bruder wohnt in Berlin.', estimatedDifficulty: 1, confidence: 0.96 },
    { sourceText: 'odgojiti', primaryTranslation: 'erziehen', cardType: 'VERB', exampleSentence: 'Sie erzieht ihre Kinder allein.', estimatedDifficulty: 3, confidence: 0.86 },
    { sourceText: 'unuk', primaryTranslation: 'der Enkel', cardType: 'NOUN', exampleSentence: 'Der Enkel besucht die Großeltern oft.', estimatedDifficulty: 2, confidence: 0.89 },
  ],
};

const GENERIC_POOL: AiGeneratedCard[] = [
  { sourceText: 'vrijeme', primaryTranslation: 'die Zeit', cardType: 'NOUN', exampleSentence: 'Ich habe keine Zeit.', estimatedDifficulty: 2, confidence: 0.9 },
  { sourceText: 'razumjeti', primaryTranslation: 'verstehen', cardType: 'VERB', exampleSentence: 'Ich verstehe dich nicht.', estimatedDifficulty: 2, confidence: 0.92 },
  { sourceText: 'lijep', primaryTranslation: 'schön', cardType: 'WORD', exampleSentence: 'Das ist ein schönes Haus.', estimatedDifficulty: 1, confidence: 0.95 },
  { sourceText: 'moliti', primaryTranslation: 'bitten', cardType: 'VERB', exampleSentence: 'Darf ich dich um etwas bitten?', estimatedDifficulty: 3, confidence: 0.84 },
  { sourceText: 'nažalost', primaryTranslation: 'leider', cardType: 'WORD', exampleSentence: 'Leider habe ich keine Zeit.', estimatedDifficulty: 3, confidence: 0.87 },
  { sourceText: 'svaki dan', primaryTranslation: 'jeden Tag', cardType: 'PHRASE', exampleSentence: 'Ich lerne jeden Tag Deutsch.', estimatedDifficulty: 1, confidence: 0.96 },
];

const RECOMMENDATION_REASONS = [
  'Low success rate in your last 3 games',
  "Hasn't been reviewed in over 2 weeks",
  'Frequently confused with a similar card',
  'Marked difficult during your last session',
];

/**
 * Frontend-only AI assistant layer. There is no backend AI service, so
 * responses are simulated from curated demo pools with an artificial delay
 * (and an occasional simulated failure) so the UI exercises real loading and
 * error states rather than always succeeding instantly.
 */
@Injectable({ providedIn: 'root' })
export class AiAssistantService {
  generateVocabulary(topic: string, level: string): Observable<AiGeneratedCard[]> {
    const normalizedTopic = topic.trim().toLowerCase();
    const matchedKey = Object.keys(TOPIC_POOLS).find((key) => normalizedTopic.includes(key));
    const pool = matchedKey ? TOPIC_POOLS[matchedKey] : GENERIC_POOL;
    const difficultyBias = level === 'A1' ? -1 : level === 'B1' ? 1 : 0;

    const generated = pool.map((card) => ({
      ...card,
      estimatedDifficulty: Math.min(5, Math.max(1, card.estimatedDifficulty + difficultyBias)),
    }));

    return this.simulate(generated, 1200, 0.12, 'The AI generation service is temporarily unavailable.');
  }

  getRecommendations(dictionaryId: number): Observable<AiRecommendation[]> {
    const recommendations = SAMPLE_CARDS.slice(0, 3).map((card, index) => ({
      cardId: card.id,
      sourceText: card.sourceText,
      reason: RECOMMENDATION_REASONS[(dictionaryId + index) % RECOMMENDATION_REASONS.length],
    }));

    return this.simulate(recommendations, 500, 0.1, "Couldn't load AI recommendations right now.");
  }

  private simulate<T>(value: T, ms: number, failureRate: number, message: string): Observable<T> {
    if (Math.random() < failureRate) {
      return throwError(() => new Error(message)).pipe(delay(ms));
    }
    return of(value).pipe(delay(ms));
  }
}
