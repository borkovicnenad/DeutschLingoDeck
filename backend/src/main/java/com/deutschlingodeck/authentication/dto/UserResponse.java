package com.deutschlingodeck.authentication.dto;

public record UserResponse(
		Long id,
		String email,
		String displayName
) {
}
