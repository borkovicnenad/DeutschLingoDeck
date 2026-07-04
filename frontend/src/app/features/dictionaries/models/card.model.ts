import { CardType } from './card-type.enum';

export interface CardSummary {
  id: number;
  cardType: CardType;
  article?: string | null;
  sourceText: string;
  primaryTranslation?: string;
  position: number;
}

export interface CardDetail extends CardSummary {
  acceptedAnswers?: string[];
  example?: string | null;
  notes?: string | null;
  difficultyLevel?: number | null;
}
