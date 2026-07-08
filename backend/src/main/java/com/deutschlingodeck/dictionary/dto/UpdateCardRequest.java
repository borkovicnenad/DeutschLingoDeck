package com.deutschlingodeck.dictionary.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateCardRequest(
		@NotNull CardType cardType,
		@Size(max = 20) String article,
		@NotBlank @Size(max = 255) String sourceText,
		@Size(max = 255) String primaryTranslation,
		List<String> acceptedAnswers,
		@Size(max = 1000) String example,
		@Size(max = 1000) String grammarInfo,
		@Min(1) @Max(5) Integer difficultyLevel,
		List<String> tags
) {
}
