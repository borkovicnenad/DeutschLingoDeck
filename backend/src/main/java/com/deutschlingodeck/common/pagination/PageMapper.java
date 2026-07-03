package com.deutschlingodeck.common.pagination;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Converts Spring Data {@link Page} results into the API's {@link PageResponse}
 * envelope. Kept as a small static helper since it is a pure, stateless
 * mapping used identically across every paged endpoint.
 */
public final class PageMapper {

	private PageMapper() {
	}

	public static <S, T> PageResponse<T> toPageResponse(Page<S> page, Function<S, T> itemMapper) {
		List<T> content = page.getContent().stream().map(itemMapper).toList();
		return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
	}
}
