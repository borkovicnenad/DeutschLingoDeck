package com.deutschlingodeck.game.dto;

import java.util.List;

/** The full, unredacted card - safe to send once the user has already submitted an answer. */
public record CardRevealResponse(
		String article,
		String sourceText,
		String primaryTranslation,
		String example,
		String grammarInfo,
		Integer difficultyLevel,
		List<String> tags
) {
}
