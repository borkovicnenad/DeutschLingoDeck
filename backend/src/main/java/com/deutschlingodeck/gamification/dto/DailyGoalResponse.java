package com.deutschlingodeck.gamification.dto;

public record DailyGoalResponse(
		Integer targetCards,
		Integer completedCards
) {
}
