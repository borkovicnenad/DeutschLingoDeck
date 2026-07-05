package com.deutschlingodeck.authentication.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Persisted record of an issued refresh token, identified by the {@code jti}
 * ("token ID") claim embedded in the corresponding JWT. The raw JWT itself is
 * never stored - only this correlation row, so a refresh token can be
 * revoked (logout, rotation) without keeping the signed token around.
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "token_id", nullable = false, unique = true)
	private UUID tokenId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "expires_at", nullable = false)
	private OffsetDateTime expiresAt;

	@Column(nullable = false)
	private boolean revoked;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	protected RefreshToken() {
	}

	public RefreshToken(UUID tokenId, Long userId, OffsetDateTime expiresAt) {
		this.tokenId = tokenId;
		this.userId = userId;
		this.expiresAt = expiresAt;
		this.revoked = false;
		this.createdAt = OffsetDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public UUID getTokenId() {
		return tokenId;
	}

	public Long getUserId() {
		return userId;
	}

	public OffsetDateTime getExpiresAt() {
		return expiresAt;
	}

	public boolean isRevoked() {
		return revoked;
	}

	public void setRevoked(boolean revoked) {
		this.revoked = revoked;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public boolean isValid() {
		return !revoked && expiresAt.isAfter(OffsetDateTime.now());
	}
}
