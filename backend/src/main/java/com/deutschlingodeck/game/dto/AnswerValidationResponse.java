package com.deutschlingodeck.game.dto;

public record AnswerValidationResponse(
		ValidationResult result,
		Boolean correct,
		String givenAnswer,
		String expectedAnswer,
		String message,
		CardRevealResponse revealedCard,
		CurrentCardResponse nextCard
) {
}
