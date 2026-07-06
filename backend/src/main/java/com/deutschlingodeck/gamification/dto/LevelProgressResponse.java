package com.deutschlingodeck.gamification.dto;

public record LevelProgressResponse(
		Integer level,
		String title,
		Long currentXp,
		Long xpToNextLevel
) {
}
