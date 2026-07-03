package com.deutschlingodeck.statistics.dto;

public record DashboardStatisticsResponse(
		Integer totalGames,
		Integer totalAnswers,
		Integer totalCorrectAnswers,
		Integer totalIncorrectAnswers,
		Double overallAccuracy,
		Long totalStudyTimeSeconds,
		Integer currentLearningStreak,
		Integer longestLearningStreak
) {
}
