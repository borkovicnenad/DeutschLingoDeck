package com.deutschlingodeck.gamification.dto;

/**
 * Hardcoded achievement registry (rule-based, not database-driven) evaluated after every
 * finished game by {@code GamificationServiceImpl}. Mirrors the frontend's previous
 * {@code SAMPLE_ACHIEVEMENTS} demo set, minus "Grammar Guru" (not computable from persisted
 * data without a bespoke irregular-verb taxonomy).
 */
public enum AchievementDefinition {
	FIRST_STEPS("First Steps", "Complete your first learning game.", "flag"),
	WEEK_STREAK("7-Day Streak", "Practice for 7 days in a row.", "local_fire_department"),
	HUNDRED_MASTERED("100 Cards Mastered", "Reach mastery on 100 vocabulary cards.", "workspace_premium"),
	PERFECT_GAME("Perfect Game", "Finish a game with 100% accuracy.", "military_tech"),
	NIGHT_OWL("Night Owl", "Complete a learning session after midnight.", "bedtime"),
	POLYGLOT_PACE("Polyglot Pace", "Answer 50 cards in a single day.", "bolt");

	private final String title;
	private final String description;
	private final String icon;

	AchievementDefinition(String title, String description, String icon) {
		this.title = title;
		this.description = description;
		this.icon = icon;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getIcon() {
		return icon;
	}
}
