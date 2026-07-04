import {
  CardStatistics,
  DashboardStatistics,
  DictionaryStatistics,
  LearningHistoryEntry,
} from './models/statistics.model';

/**
 * Static placeholder content shown immediately on page load and kept on
 * screen if the live request fails, so the Statistics feature (and the
 * Dashboard, which reuses the same overview shape) is always navigable and
 * demonstrates its intended UX. Replaced by real data as soon as the
 * backend responds successfully.
 */
export const SAMPLE_DASHBOARD_STATISTICS: DashboardStatistics = {
  totalGames: 24,
  totalAnswers: 412,
  totalCorrectAnswers: 338,
  totalIncorrectAnswers: 74,
  overallAccuracy: 0.82,
  totalStudyTimeSeconds: 15840,
  currentLearningStreak: 5,
  longestLearningStreak: 11,
};

export const SAMPLE_DICTIONARY_STATISTICS: DictionaryStatistics[] = [
  {
    dictionaryId: 9001,
    dictionaryName: 'Everyday Essentials',
    completedGames: 12,
    totalAnswers: 210,
    averageAccuracy: 0.86,
    masteredCards: 74,
    difficultCards: 9,
  },
  {
    dictionaryId: 9002,
    dictionaryName: 'Business German',
    completedGames: 6,
    totalAnswers: 98,
    averageAccuracy: 0.79,
    masteredCards: 31,
    difficultCards: 6,
  },
  {
    dictionaryId: 9003,
    dictionaryName: 'Travel Phrases',
    completedGames: 4,
    totalAnswers: 64,
    averageAccuracy: 0.9,
    masteredCards: 28,
    difficultCards: 2,
  },
];

export const SAMPLE_CARD_STATISTICS: CardStatistics[] = [
  {
    cardId: 9101,
    dictionaryId: 9001,
    sourceText: 'kuća',
    timesShown: 14,
    timesCorrect: 12,
    timesIncorrect: 2,
    successRate: 0.86,
    lastShownAt: '2026-07-01T18:20:00Z',
    lastCorrectAt: '2026-07-01T18:20:00Z',
  },
  {
    cardId: 9102,
    dictionaryId: 9001,
    sourceText: 'govoriti',
    timesShown: 9,
    timesCorrect: 5,
    timesIncorrect: 4,
    successRate: 0.56,
    lastShownAt: '2026-06-29T10:05:00Z',
    lastCorrectAt: '2026-06-27T09:40:00Z',
  },
  {
    cardId: 9103,
    dictionaryId: 9001,
    sourceText: 'grad',
    timesShown: 6,
    timesCorrect: 6,
    timesIncorrect: 0,
    successRate: 1,
    lastShownAt: '2026-06-25T12:00:00Z',
    lastCorrectAt: '2026-06-25T12:00:00Z',
  },
];

export const SAMPLE_LEARNING_HISTORY: LearningHistoryEntry[] = [
  {
    gameId: 9500,
    dictionaryId: 9001,
    dictionaryName: 'Everyday Essentials',
    status: 'FINISHED',
    startedAt: '2026-07-01T18:10:00Z',
    finishedAt: '2026-07-01T18:15:00Z',
    accuracy: 0.85,
  },
  {
    gameId: 9499,
    dictionaryId: 9002,
    dictionaryName: 'Business German',
    status: 'FINISHED',
    startedAt: '2026-06-29T09:55:00Z',
    finishedAt: '2026-06-29T10:00:00Z',
    accuracy: 0.73,
  },
  {
    gameId: 9498,
    dictionaryId: 9003,
    dictionaryName: 'Travel Phrases',
    status: 'ABANDONED',
    startedAt: '2026-06-25T11:55:00Z',
    finishedAt: null,
    accuracy: 0.67,
  },
];
