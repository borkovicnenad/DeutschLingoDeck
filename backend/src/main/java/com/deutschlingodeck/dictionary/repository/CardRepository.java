package com.deutschlingodeck.dictionary.repository;

import com.deutschlingodeck.dictionary.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

	Optional<Card> findByIdAndDictionaryId(Long id, Long dictionaryId);

	List<Card> findByDictionaryIdOrderByPosition(Long dictionaryId);

	Page<Card> findByDictionaryId(Long dictionaryId, Pageable pageable);

	Page<Card> findByDictionaryOwnerId(Long ownerId, Pageable pageable);

	int countByDictionaryId(Long dictionaryId);

	int countByDictionaryOwnerId(Long ownerId);

	@Query("SELECT COALESCE(MAX(c.position), -1) FROM Card c WHERE c.dictionary.id = :dictionaryId")
	int findMaxPositionByDictionaryId(@Param("dictionaryId") Long dictionaryId);

	/**
	 * Filters a dictionary's cards by optional tag/difficulty/free-text search, and by the
	 * current user's {@code CardProgress} status ({@code new}: never reviewed,
	 * {@code due}: no progress yet or due today/earlier, {@code mastered}: interval past the
	 * mastery threshold). {@code CardProgress} is joined ad-hoc (it has no mapped relationship
	 * to {@code Card}) so the filter can be scoped to the requesting user.
	 */
	@Query(value = """
			SELECT DISTINCT c FROM Card c
			LEFT JOIN c.tags t
			LEFT JOIN CardProgress cp ON cp.card = c AND cp.user.id = :userId
			WHERE c.dictionary.id = :dictionaryId
			  AND (:tag IS NULL OR t = :tag)
			  AND (:difficulty IS NULL OR c.difficultyLevel = :difficulty)
			  AND (:search IS NULL OR LOWER(c.sourceText) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
			       OR LOWER(c.primaryTranslation) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
			  AND (
			    :status IS NULL
			    OR (:status = 'new' AND cp.id IS NULL)
			    OR (:status = 'due' AND (cp.id IS NULL OR cp.dueDate <= CURRENT_DATE))
			    OR (:status = 'mastered' AND cp.intervalDays >= :masteredThreshold)
			  )
			ORDER BY c.position ASC
			""",
			countQuery = """
			SELECT COUNT(DISTINCT c) FROM Card c
			LEFT JOIN c.tags t
			LEFT JOIN CardProgress cp ON cp.card = c AND cp.user.id = :userId
			WHERE c.dictionary.id = :dictionaryId
			  AND (:tag IS NULL OR t = :tag)
			  AND (:difficulty IS NULL OR c.difficultyLevel = :difficulty)
			  AND (:search IS NULL OR LOWER(c.sourceText) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
			       OR LOWER(c.primaryTranslation) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
			  AND (
			    :status IS NULL
			    OR (:status = 'new' AND cp.id IS NULL)
			    OR (:status = 'due' AND (cp.id IS NULL OR cp.dueDate <= CURRENT_DATE))
			    OR (:status = 'mastered' AND cp.intervalDays >= :masteredThreshold)
			  )
			""")
	Page<Card> search(
			@Param("dictionaryId") Long dictionaryId,
			@Param("userId") Long userId,
			@Param("tag") String tag,
			@Param("difficulty") Integer difficulty,
			@Param("status") String status,
			@Param("search") String search,
			@Param("masteredThreshold") int masteredThreshold,
			Pageable pageable);
}
