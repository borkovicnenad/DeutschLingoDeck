import { CardType } from '../../dictionaries/models/card-type.enum';
import { GameStatus } from './game-status.enum';
import { ValidationResult } from './validation-result.enum';

export interface CurrentCard {
  id: number;
  sourceText: string;
  cardType: CardType;
  article?: string | null;
  example?: string | null;
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
