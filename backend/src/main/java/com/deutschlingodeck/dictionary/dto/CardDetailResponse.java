package com.deutschlingodeck.dictionary.dto;

import java.util.List;

/**
 * Flattened form of {@code CardDetailResponse} (allOf
 * {@code CardSummaryResponse} + example/notes/etc.) from the OpenAPI spec.
 */
public record CardDetailResponse(
		Long id,
		CardType cardType,
		String article,
		String sourceText,
		String primaryTranslation,
		Integer position,
		List<String> acceptedAnswers,
		String example,
		String notes,
		Integer difficultyLevel
) {
}
