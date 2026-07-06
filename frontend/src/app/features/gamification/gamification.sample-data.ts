import { Achievement, DailyGoal, LevelProgress, WeeklyActivityPoint } from './models/gamification.model';

/**
 * There is no backend concept of XP, levels or achievements — this entire
 * feature is a frontend-only demo layer, deliberately kept separate from the
 * models that mirror the real API so wiring a real endpoint later never
 * requires removing invented fields from those DTOs.
 */
export const SAMPLE_LEVEL_PROGRESS: LevelProgress = {
  level: 7,
  title: 'Wortschatz-Kenner',
  currentXp: 1240,
  xpToNextLevel: 1500,
};

export const SAMPLE_DAILY_GOAL: DailyGoal = {
  targetCards: 30,
  completedCards: 20,
};

export const SAMPLE_ACHIEVEMENTS: Achievement[] = [
  {
    id: 'first-steps',
    title: 'First Steps',
    description: 'Complete your first learning game.',
    icon: 'flag',
    unlocked: true,
    unlockedAt: '2026-04-05T10:00:00Z',
  },
  {
    id: 'week-streak',
    title: '7-Day Streak',
    description: 'Practice for 7 days in a row.',
    icon: 'local_fire_department',
    unlocked: true,
    unlockedAt: '2026-04-14T18:30:00Z',
  },
  {
    id: 'hundred-mastered',
    title: '100 Cards Mastered',
    description: 'Reach mastery on 100 vocabulary cards.',
    icon: 'workspace_premium',
    unlocked: true,
    unlockedAt: '2026-05-20T09:15:00Z',
  },
  {
    id: 'perfect-game',
    title: 'Perfect Game',
    description: 'Finish a game with 100% accuracy.',
    icon: 'military_tech',
    unlocked: true,
    unlockedAt: '2026-06-02T20:05:00Z',
  },
  {
    id: 'night-owl',
    title: 'Night Owl',
    description: 'Complete a learning session after midnight.',
    icon: 'bedtime',
    unlocked: false,
  },
  {
    id: 'grammar-guru',
    title: 'Grammar Guru',
    description: 'Master every irregular verb in a dictionary.',
    icon: 'auto_stories',
    unlocked: false,
  },
  {
    id: 'polyglot-pace',
    title: 'Polyglot Pace',
    description: 'Answer 50 cards in a single day.',
    icon: 'bolt',
    unlocked: false,
  },
];

export const SAMPLE_WEEKLY_ACTIVITY: WeeklyActivityPoint[] = [
  { day: 'Mon', cardsReviewed: 18 },
  { day: 'Tue', cardsReviewed: 24 },
  { day: 'Wed', cardsReviewed: 12 },
  { day: 'Thu', cardsReviewed: 30 },
  { day: 'Fri', cardsReviewed: 22 },
  { day: 'Sat', cardsReviewed: 9 },
  { day: 'Sun', cardsReviewed: 20 },
];
