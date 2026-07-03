package com.deutschlingodeck.dictionary.dto;

import java.util.List;

public record DictionaryImportResponse(
		Long dictionaryId,
		String name,
		Integer importedCards,
		String status,
		List<String> warnings
) {
}
