package com.deutschlingodeck.game.progress;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CardProgressRepository extends JpaRepository<CardProgress, Long> {

	Optional<CardProgress> findByUserIdAndCardId(Long userId, Long cardId);

	@Query("""
			SELECT cp FROM CardProgress cp
			WHERE cp.user.id = :userId AND cp.card.dictionary.id = :dictionaryId AND cp.dueDate <= :today
			""")
	List<CardProgress> findDueForDictionary(
			@Param("userId") Long userId, @Param("dictionaryId") Long dictionaryId, @Param("today") LocalDate today);

	@Query("""
			SELECT cp.card.id FROM CardProgress cp
			WHERE cp.user.id = :userId AND cp.card.dictionary.id = :dictionaryId
			""")
	List<Long> findReviewedCardIdsForDictionary(@Param("userId") Long userId, @Param("dictionaryId") Long dictionaryId);

	@Query("""
			SELECT COUNT(cp) FROM CardProgress cp
			WHERE cp.user.id = :userId AND cp.dueDate <= :today
			""")
	long countDueForUser(@Param("userId") Long userId, @Param("today") LocalDate today);

	@Query("""
			SELECT COUNT(cp) FROM CardProgress cp
			WHERE cp.user.id = :userId AND cp.card.dictionary.id = :dictionaryId AND cp.intervalDays >= :masteredThreshold
			""")
	long countMasteredForDictionary(
			@Param("userId") Long userId, @Param("dictionaryId") Long dictionaryId, @Param("masteredThreshold") int masteredThreshold);

	@Query("SELECT COUNT(cp) FROM CardProgress cp WHERE cp.user.id = :userId AND cp.intervalDays >= :masteredThreshold")
	long countMasteredForUser(@Param("userId") Long userId, @Param("masteredThreshold") int masteredThreshold);
}
