package com.deutschlingodeck.gamification.dto;

import java.time.OffsetDateTime;

public record AchievementResponse(
		String id,
		String title,
		String description,
		String icon,
		Boolean unlocked,
		OffsetDateTime unlockedAt
) {
}
