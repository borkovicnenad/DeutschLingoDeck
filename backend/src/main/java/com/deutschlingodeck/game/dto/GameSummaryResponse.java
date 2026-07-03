package com.deutschlingodeck.game.dto;

public record GameSummaryResponse(
		Long gameId,
		GameStatus status,
		Integer totalCards,
		Integer answeredCards,
		Integer correctAnswers,
		Integer incorrectAnswers,
		Double accuracy,
		Long durationSeconds,
		Double averageResponseTimeMs
) {
}
