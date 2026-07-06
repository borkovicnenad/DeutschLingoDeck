export interface LevelProgress {
  level: number;
  title: string;
  currentXp: number;
  xpToNextLevel: number;
}

export interface Achievement {
  id: string;
  title: string;
  description: string;
  icon: string;
  unlocked: boolean;
  unlockedAt?: string;
}

export interface DailyGoal {
  targetCards: number;
  completedCards: number;
}

export interface WeeklyActivityPoint {
  day: string;
  cardsReviewed: number;
}
