package com.deutschlingodeck.game.repository;

import com.deutschlingodeck.game.entity.GameAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface GameAnswerRepository extends JpaRepository<GameAnswer, Long> {

	List<GameAnswer> findByGameId(Long gameId);

	@Query("SELECT AVG(a.responseTimeMs) FROM GameAnswer a WHERE a.game.id = :gameId")
	Double averageResponseTimeMsByGame(@Param("gameId") Long gameId);

	@Query("""
			SELECT COUNT(a) FROM GameAnswer a
			WHERE a.game.user.id = :userId AND a.answeredAt >= :start AND a.answeredAt < :end
			""")
	long countByUserAndAnsweredAtBetween(
			@Param("userId") Long userId, @Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);

	@Query("""
			SELECT a FROM GameAnswer a
			WHERE a.game.user.id = :userId AND a.answeredAt >= :start AND a.answeredAt < :end
			""")
	List<GameAnswer> findByUserAndAnsweredAtBetween(
			@Param("userId") Long userId, @Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);

	@Query("SELECT COUNT(a) FROM GameAnswer a WHERE a.game.user.id = :userId AND a.game.dictionary.id = :dictionaryId")
	long countByUserAndDictionary(@Param("userId") Long userId, @Param("dictionaryId") Long dictionaryId);

	@Query("""
			SELECT COUNT(a) FROM GameAnswer a
			WHERE a.game.user.id = :userId AND a.game.dictionary.id = :dictionaryId AND a.correct = true
			""")
	long countCorrectByUserAndDictionary(@Param("userId") Long userId, @Param("dictionaryId") Long dictionaryId);

	/** One row per card in {@code cardIds} that has at least one answer from this user. */
	@Query("""
			SELECT new com.deutschlingodeck.game.repository.CardAnswerAggregate(
			    a.card.id, COUNT(a), SUM(CASE WHEN a.correct = true THEN 1L ELSE 0L END),
			    MAX(a.answeredAt), MAX(CASE WHEN a.correct = true THEN a.answeredAt ELSE NULL END))
			FROM GameAnswer a
			WHERE a.game.user.id = :userId AND a.card.id IN :cardIds
			GROUP BY a.card.id
			""")
	List<CardAnswerAggregate> aggregateForCards(@Param("userId") Long userId, @Param("cardIds") List<Long> cardIds);
}
