package com.deutschlingodeck.gamification.service;

import com.deutschlingodeck.gamification.dto.AchievementResponse;
import com.deutschlingodeck.gamification.dto.DailyGoalResponse;
import com.deutschlingodeck.gamification.dto.LevelProgressResponse;
import com.deutschlingodeck.gamification.dto.WeeklyActivityPointResponse;

import java.time.OffsetDateTime;
import java.util.List;

public interface GamificationService {

	/** Called once per submitted answer: awards XP and updates the daily streak. */
	void recordAnswer(Long userId, boolean correct, OffsetDateTime answeredAt);

	/** Called once per finished game: unlocks any newly-earned achievements. */
	void evaluateAchievements(Long userId, boolean perfectGame, OffsetDateTime finishedAt);

	LevelProgressResponse getLevelProgress(Long userId);

	DailyGoalResponse getDailyGoal(Long userId);

	List<AchievementResponse> getAchievements(Long userId);

	List<WeeklyActivityPointResponse> getWeeklyActivity(Long userId);
}
