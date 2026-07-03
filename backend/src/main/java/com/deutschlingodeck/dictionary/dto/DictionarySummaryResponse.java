package com.deutschlingodeck.dictionary.dto;

import java.time.OffsetDateTime;

public record DictionarySummaryResponse(
		Long id,
		String name,
		String description,
		String sourceLanguage,
		String targetLanguage,
		Integer cardCount,
		OffsetDateTime createdAt
) {
}
