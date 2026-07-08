package com.deutschlingodeck.game.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** {@code answer} may be an empty string - the frontend's Skip action submits "" as an intentional, incorrect answer. */
public record SubmitAnswerRequest(
		@NotNull Long cardId,
		@NotNull @Size(max = 500) String answer,
		@NotNull @PositiveOrZero Long responseTimeMs
) {
}
