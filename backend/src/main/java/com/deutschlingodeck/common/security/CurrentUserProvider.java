package com.deutschlingodeck.common.security;

import com.deutschlingodeck.common.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolves the current user's id from the {@link SecurityContextHolder}, where
 * {@code JwtAuthenticationFilter} places it as the raw authentication principal.
 */
@Component
public class CurrentUserProvider {

	public Long getUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
			throw new UnauthorizedException("Authentication is required");
		}
		return userId;
	}
}
