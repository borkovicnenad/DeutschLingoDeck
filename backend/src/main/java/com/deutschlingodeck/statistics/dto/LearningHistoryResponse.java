package com.deutschlingodeck.statistics.dto;

import com.deutschlingodeck.game.dto.GameStatus;

import java.time.OffsetDateTime;

public record LearningHistoryResponse(
		Long gameId,
		Long dictionaryId,
		String dictionaryName,
		GameStatus status,
		OffsetDateTime startedAt,
		OffsetDateTime finishedAt,
		Double accuracy
) {
}
