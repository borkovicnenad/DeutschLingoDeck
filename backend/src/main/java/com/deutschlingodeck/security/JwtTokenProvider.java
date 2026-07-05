package com.deutschlingodeck.security;

import com.deutschlingodeck.authentication.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Issues and validates the two JWT types used by the API:
 * <ul>
 *   <li>access tokens - short-lived, sent as {@code Authorization: Bearer},
 *       carry the user id/email/displayName as claims;</li>
 *   <li>refresh tokens - longer-lived, carry only a {@code jti} ("token ID")
 *       that correlates to a {@link com.deutschlingodeck.authentication.entity.RefreshToken}
 *       row, so a token can be revoked without keeping the JWT itself.</li>
 * </ul>
 * Both are signed HS256 with the same secret; the {@code type} claim keeps
 * one from being used in place of the other.
 */
@Component
public class JwtTokenProvider {

	private static final String CLAIM_TYPE = "type";
	private static final String TYPE_ACCESS = "access";
	private static final String TYPE_REFRESH = "refresh";

	private final SecretKey key;
	private final JwtProperties properties;

	public JwtTokenProvider(JwtProperties properties) {
		this.properties = properties;
		this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
	}

	public String generateAccessToken(User user) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(user.getId().toString())
				.claim("email", user.getEmail())
				.claim("displayName", user.getDisplayName())
				.claim(CLAIM_TYPE, TYPE_ACCESS)
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plus(properties.accessTokenTtl())))
				.signWith(key)
				.compact();
	}

	public String generateRefreshToken(User user, UUID tokenId) {
		Instant now = Instant.now();
		return Jwts.builder()
				.id(tokenId.toString())
				.subject(user.getId().toString())
				.claim(CLAIM_TYPE, TYPE_REFRESH)
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plus(properties.refreshTokenTtl())))
				.signWith(key)
				.compact();
	}

	/** @throws io.jsonwebtoken.JwtException if the token is malformed, expired, or has an invalid signature. */
	public Jws<Claims> parseToken(String token) {
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
	}

	public boolean isAccessToken(Claims claims) {
		return TYPE_ACCESS.equals(claims.get(CLAIM_TYPE, String.class));
	}

	public boolean isRefreshToken(Claims claims) {
		return TYPE_REFRESH.equals(claims.get(CLAIM_TYPE, String.class));
	}

	public Duration getRefreshTokenTtl() {
		return properties.refreshTokenTtl();
	}
}
