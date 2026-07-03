package com.deutschlingodeck.dictionary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

/**
 * Backing object for the {@code multipart/form-data} request on
 * {@code POST /dictionaries/import}.
 */
public record DictionaryImportRequest(
		@NotNull MultipartFile file,
		@NotBlank @Size(min = 1, max = 150) String name,
		@Size(max = 1000) String description,
		@NotBlank String sourceLanguage,
		@NotBlank String targetLanguage
) {
}
