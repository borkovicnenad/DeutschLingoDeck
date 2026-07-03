package com.deutschlingodeck.common.pagination;

import java.util.List;

/**
 * Flattened page envelope matching the {@code allOf: [PageResponse, ...]}
 * pattern used for every paged schema in {@code docs/openapi.yaml}
 * (e.g. {@code PagedDictionarySummaryResponse}). Used generically as
 * {@code PageResponse<DictionarySummaryResponse>} etc. so the serialized
 * JSON shape matches the spec exactly.
 */
public record PageResponse<T>(
		List<T> content,
		int page,
		int size,
		long totalElements,
		int totalPages
) {
}
