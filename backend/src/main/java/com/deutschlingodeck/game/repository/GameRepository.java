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
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {

	long countByUserIdAndStatus(Long userId, GameStatus status);

	long countByUserIdAndDictionaryIdAndStatus(Long userId, Long dictionaryId, GameStatus status);

	Optional<Game> findByUserIdAndStatus(Long userId, GameStatus status);

	List<Game> findByUserIdAndStartedAtBetween(Long userId, OffsetDateTime start, OffsetDateTime end);

	Page<Game> findByUserIdAndStartedAtBetween(Long userId, OffsetDateTime start, OffsetDateTime end, Pageable pageable);

	List<Game> findByStatusAndLastActivityAtBefore(GameStatus status, OffsetDateTime cutoff);

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

	/** Backs both the dashboard overview and the Learning History table so their filters stay in sync. */
	@Query("""
			SELECT g FROM Game g
			WHERE g.user.id = :userId
			  AND g.startedAt >= :start AND g.startedAt <= :end
			  AND (:dictionaryId IS NULL OR g.dictionary.id = :dictionaryId)
			  AND (:gameMode IS NULL OR g.gameMode = :gameMode)
			""")
	List<Game> findForStatistics(
			@Param("userId") Long userId,
			@Param("start") OffsetDateTime start,
			@Param("end") OffsetDateTime end,
			@Param("dictionaryId") Long dictionaryId,
			@Param("gameMode") String gameMode);

	@Query("""
			SELECT g FROM Game g
			WHERE g.user.id = :userId
			  AND g.startedAt >= :start AND g.startedAt <= :end
			  AND (:dictionaryId IS NULL OR g.dictionary.id = :dictionaryId)
			  AND (:gameMode IS NULL OR g.gameMode = :gameMode)
			ORDER BY g.startedAt DESC
			""")
	Page<Game> findForStatistics(
			@Param("userId") Long userId,
			@Param("start") OffsetDateTime start,
			@Param("end") OffsetDateTime end,
			@Param("dictionaryId") Long dictionaryId,
			@Param("gameMode") String gameMode,
			Pageable pageable);
}
