package com.deutschlingodeck.game.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SubmitAnswerRequest(
		@NotNull Long cardId,
		@NotBlank @Size(min = 1, max = 500) String answer,
		@NotNull @PositiveOrZero Long responseTimeMs
) {
}
