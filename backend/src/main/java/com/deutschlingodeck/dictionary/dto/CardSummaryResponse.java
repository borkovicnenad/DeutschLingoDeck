package com.deutschlingodeck.dictionary.dto;

public record CardSummaryResponse(
		Long id,
		CardType cardType,
		String article,
		String sourceText,
		String primaryTranslation,
		Integer position
) {
}
