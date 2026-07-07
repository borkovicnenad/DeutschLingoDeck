package com.deutschlingodeck.security;

/**
 * Endpoints that must remain accessible without authentication.
 */
public final class SecurityConstants {

	public static final String[] PUBLIC_ENDPOINTS = {
			"/api/v1/auth/register",
			"/api/v1/auth/login",
			"/api/v1/auth/refresh",
			"/swagger-ui.html",
			"/swagger-ui/**",
			"/v3/api-docs/**",
			"/actuator/health"
	};

	private SecurityConstants() {
	}
}
