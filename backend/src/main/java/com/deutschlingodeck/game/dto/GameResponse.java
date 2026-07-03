package com.deutschlingodeck.game.dto;

public record GameResponse(
		Long id,
		Long dictionaryId,
		GameStatus status,
		Integer totalCards,
		Integer answeredCards,
		Integer correctAnswers,
		Integer incorrectAnswers,
		CurrentCardResponse currentCard
) {
}
