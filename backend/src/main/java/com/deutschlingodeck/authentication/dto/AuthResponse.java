package com.deutschlingodeck.authentication.dto;

public record AuthResponse(
		String accessToken,
		String refreshToken,
		String tokenType,
		UserResponse user
) {
}
