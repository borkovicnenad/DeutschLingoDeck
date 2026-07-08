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
  grammarInfo?: string | null;
  difficultyLevel?: number | null;
  tags?: string[];
}

export interface CardRequest {
  cardType: CardType;
  article?: string | null;
  sourceText: string;
  primaryTranslation?: string;
  acceptedAnswers?: string[];
  example?: string | null;
  grammarInfo?: string | null;
  difficultyLevel?: number | null;
  tags?: string[];
}

export type CardFilterStatus = 'new' | 'due' | 'mastered';

export interface CardFilter {
  tag?: string;
  difficulty?: number;
  status?: CardFilterStatus;
  search?: string;
}
