import { CardType } from '../../dictionaries/models/card-type.enum';
import { GameStatus } from './game-status.enum';
import { ValidationResult } from './validation-result.enum';

/**
 * The question side of a card, redacted by the backend according to the dictionary's
 * learning direction: whichever field is the "answer" for this direction is `null`/absent
 * until the answer is submitted (see `revealedCard` on `AnswerValidationResponse`).
 */
export interface CurrentCard {
  id: number;
  cardType: CardType;
  article?: string | null;
  sourceText?: string | null;
  primaryTranslation?: string | null;
  example?: string | null;
  grammarInfo?: string | null;
}

/** The full, unredacted card - safe to render once the user has already submitted an answer. */
export interface CardReveal {
  article?: string | null;
  sourceText: string;
  primaryTranslation?: string | null;
  example?: string | null;
  grammarInfo?: string | null;
  difficultyLevel?: number | null;
  tags?: string[];
}

export interface Game {
  id: number;
  dictionaryId: number;
  status: GameStatus;
  totalCards: number;
  answeredCards: number;
  correctAnswers?: number;
  incorrectAnswers?: number;
  currentCard?: CurrentCard;
}

export interface CreateGameRequest {
  dictionaryId: number;
  gameMode?: string;
}

export interface SubmitAnswerRequest {
  cardId: number;
  answer: string;
  responseTimeMs: number;
}

export interface AnswerValidationResponse {
  result: ValidationResult;
  correct: boolean;
  givenAnswer: string;
  expectedAnswer: string;
  message?: string;
  revealedCard?: CardReveal;
  nextCard?: CurrentCard;
}

export interface GameSummary {
  gameId: number;
  status: GameStatus;
  totalCards: number;
  answeredCards?: number;
  correctAnswers: number;
  incorrectAnswers: number;
  accuracy: number;
  durationSeconds?: number;
  averageResponseTimeMs?: number;
}
