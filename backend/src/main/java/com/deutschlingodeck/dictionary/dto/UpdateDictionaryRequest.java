package com.deutschlingodeck.dictionary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateDictionaryRequest(
		@NotBlank @Size(max = 150) String name,
		@Size(max = 1000) String description,
		@NotBlank String sourceLanguage,
		@NotBlank String targetLanguage
) {
}
