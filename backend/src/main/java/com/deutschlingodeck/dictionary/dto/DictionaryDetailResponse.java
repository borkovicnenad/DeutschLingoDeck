package com.deutschlingodeck.dictionary.dto;

import java.time.OffsetDateTime;

/**
 * Flattened form of {@code DictionaryDetailResponse} (allOf
 * {@code DictionarySummaryResponse} + {@code updatedAt}) from the OpenAPI spec.
 */
public record DictionaryDetailResponse(
		Long id,
		String name,
		String description,
		String sourceLanguage,
		String targetLanguage,
		Integer cardCount,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {
}
