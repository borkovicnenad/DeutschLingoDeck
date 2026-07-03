package com.deutschlingodeck.statistics.dto;

import java.time.OffsetDateTime;

public record CardStatisticsResponse(
		Long cardId,
		Long dictionaryId,
		String sourceText,
		Integer timesShown,
		Integer timesCorrect,
		Integer timesIncorrect,
		Double successRate,
		OffsetDateTime lastShownAt,
		OffsetDateTime lastCorrectAt
) {
}
