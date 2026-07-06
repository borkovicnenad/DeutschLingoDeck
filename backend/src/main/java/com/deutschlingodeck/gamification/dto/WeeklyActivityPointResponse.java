package com.deutschlingodeck.gamification.dto;

public record WeeklyActivityPointResponse(
		String day,
		Integer cardsReviewed
) {
}
