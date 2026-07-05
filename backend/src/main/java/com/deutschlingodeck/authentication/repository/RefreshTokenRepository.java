package com.deutschlingodeck.authentication.repository;

import com.deutschlingodeck.authentication.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByTokenId(UUID tokenId);

	@Modifying
	@Query("update RefreshToken t set t.revoked = true where t.userId = :userId and t.revoked = false")
	void revokeAllActiveForUser(@Param("userId") Long userId);
}
