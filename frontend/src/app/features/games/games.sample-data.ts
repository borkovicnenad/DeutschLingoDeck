import { Game, GameSummary } from './models/game.model';

/**
 * Static placeholder content shown immediately on page load and kept on
 * screen if the live request fails, so the Games feature is always
 * navigable and demonstrates its intended UX. Replaced by real data as soon
 * as the backend responds successfully.
 */
export const SAMPLE_ACTIVE_GAME: Game = {
  id: 9501,
  dictionaryId: 9001,
  status: 'IN_PROGRESS',
  totalCards: 20,
  answeredCards: 7,
  correctAnswers: 5,
  incorrectAnswers: 2,
  currentCard: {
    id: 9102,
    sourceText: 'govoriti',
    cardType: 'VERB',
    example: 'Ich spreche Deutsch.',
  },
};

export const SAMPLE_GAME_SUMMARY: GameSummary = {
  gameId: 9500,
  status: 'FINISHED',
  totalCards: 20,
  answeredCards: 20,
  correctAnswers: 17,
  incorrectAnswers: 3,
  accuracy: 0.85,
  durationSeconds: 312,
  averageResponseTimeMs: 2860,
};

export const SAMPLE_GAME_HISTORY: GameSummary[] = [
  {
    gameId: 9500,
    status: 'FINISHED',
    totalCards: 20,
    answeredCards: 20,
    correctAnswers: 17,
    incorrectAnswers: 3,
    accuracy: 0.85,
    durationSeconds: 312,
    averageResponseTimeMs: 2860,
  },
  {
    gameId: 9499,
    status: 'FINISHED',
    totalCards: 15,
    answeredCards: 15,
    correctAnswers: 11,
    incorrectAnswers: 4,
    accuracy: 0.73,
    durationSeconds: 244,
    averageResponseTimeMs: 3120,
  },
  {
    gameId: 9498,
    status: 'ABANDONED',
    totalCards: 30,
    answeredCards: 9,
    correctAnswers: 6,
    incorrectAnswers: 3,
    accuracy: 0.67,
    durationSeconds: 96,
    averageResponseTimeMs: 2600,
  },
];
