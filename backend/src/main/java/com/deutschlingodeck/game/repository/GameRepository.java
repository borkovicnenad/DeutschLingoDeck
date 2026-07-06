package com.deutschlingodeck.game.repository;

import com.deutschlingodeck.game.dto.GameStatus;
import com.deutschlingodeck.game.entity.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {

	long countByUserIdAndStatus(Long userId, GameStatus status);

	long countByUserIdAndDictionaryIdAndStatus(Long userId, Long dictionaryId, GameStatus status);

	List<Game> findByUserIdAndStartedAtBetween(Long userId, OffsetDateTime start, OffsetDateTime end);

	Page<Game> findByUserIdAndStartedAtBetween(Long userId, OffsetDateTime start, OffsetDateTime end, Pageable pageable);

	@Query("""
			SELECT g FROM Game g
			WHERE g.user.id = :userId
			  AND (:status IS NULL OR g.status = :status)
			  AND (:dictionaryId IS NULL OR g.dictionary.id = :dictionaryId)
			ORDER BY g.startedAt DESC
			""")
	Page<Game> search(
			@Param("userId") Long userId,
			@Param("status") GameStatus status,
			@Param("dictionaryId") Long dictionaryId,
			Pageable pageable);
}
