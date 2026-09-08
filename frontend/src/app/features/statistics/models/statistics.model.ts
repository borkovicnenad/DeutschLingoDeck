export interface DashboardStatistics {
  totalGames?: number;
  totalAnswers?: number;
  totalCorrectAnswers?: number;
  totalIncorrectAnswers?: number;
  overallAccuracy?: number;
  totalStudyTimeSeconds?: number;
  currentLearningStreak?: number;
  longestLearningStreak?: number;
  cardsDueToday?: number;
}

export interface CardStatistics {
  cardId?: number;
  dictionaryId?: number;
  sourceText?: string;
  timesShown?: number;
  timesCorrect?: number;
  timesIncorrect?: number;
  successRate?: number;
  lastShownAt?: string | null;
  lastCorrectAt?: string | null;
  averageResponseTimeMs?: number | null;
}

export interface MasteryBreakdown {
  newCount?: number;
  learningCount?: number;
  familiarCount?: number;
  strongCount?: number;
  masteredCount?: number;
  totalCards?: number;
}

export interface DictionaryStatistics {
  dictionaryId?: number;
  dictionaryName?: string;
  completedGames?: number;
  totalAnswers?: number;
  averageAccuracy?: number;
  masteredCards?: number;
  difficultCards?: number;
}

export interface LearningHistoryEntry {
  gameId?: number;
  dictionaryId?: number;
  dictionaryName?: string;
  status?: string;
  startedAt?: string;
  finishedAt?: string | null;
  accuracy?: number;
}
