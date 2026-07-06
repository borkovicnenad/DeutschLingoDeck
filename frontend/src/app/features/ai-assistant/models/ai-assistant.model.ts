import { CardType } from '../../dictionaries/models/card-type.enum';

export interface AiGeneratedCard {
  sourceText: string;
  primaryTranslation: string;
  cardType: CardType;
  exampleSentence?: string;
  estimatedDifficulty: number;
  confidence: number;
}

export interface AiRecommendation {
  cardId: number;
  sourceText: string;
  reason: string;
}
