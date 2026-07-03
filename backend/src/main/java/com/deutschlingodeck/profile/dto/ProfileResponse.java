package com.deutschlingodeck.profile.dto;

import java.time.OffsetDateTime;

public record ProfileResponse(
		Long id,
		String email,
		String displayName,
		OffsetDateTime createdAt
) {
}
