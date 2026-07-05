package com.deutschlingodeck.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Populates the {@link SecurityContextHolder} from a {@code Bearer} access
 * token, if present and valid. Never rejects the request itself - an
 * absent/invalid token simply leaves the context unauthenticated, so public
 * endpoints keep working and Spring Security's own authorization check
 * (and {@link RestAuthenticationEntryPoint}) handles the 401 for protected ones.
 *
 * <p>The authenticated principal is just the user's id ({@link Long}); services
 * that need the full user re-read it from the database, which keeps
 * {@code displayName}/{@code email} in the response always current instead of
 * potentially stale for the lifetime of the access token.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtTokenProvider jwtTokenProvider;

	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (header != null && header.startsWith(BEARER_PREFIX)) {
			String token = header.substring(BEARER_PREFIX.length());
			try {
				Claims claims = jwtTokenProvider.parseToken(token).getPayload();
				if (jwtTokenProvider.isAccessToken(claims)) {
					Long userId = Long.valueOf(claims.getSubject());
					var authentication = new UsernamePasswordAuthenticationToken(userId, null, List.of());
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
			} catch (JwtException | IllegalArgumentException ex) {
				SecurityContextHolder.clearContext();
			}
		}

		filterChain.doFilter(request, response);
	}
}
