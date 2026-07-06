package com.deutschlingodeck.game.progress;

import com.deutschlingodeck.authentication.entity.User;
import com.deutschlingodeck.dictionary.entity.Card;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Per-user, per-card spaced-repetition scheduling state (SM-2-lite). Updated by
 * {@link SpacedRepetitionScheduler} every time the user answers this card in a game.
 */
@Entity
@Table(name = "card_progress")
public class CardProgress {

	/** A card is considered "mastered" once its review interval reaches this many days. */
	public static final int MASTERED_INTERVAL_DAYS_THRESHOLD = 21;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "card_id", nullable = false)
	private Card card;

	@Column(name = "ease_factor", nullable = false)
	private BigDecimal easeFactor;

	@Column(name = "interval_days", nullable = false)
	private Integer intervalDays;

	@Column(nullable = false)
	private Integer repetitions;

	@Column(name = "due_date", nullable = false)
	private LocalDate dueDate;

	@Column(name = "last_reviewed_at")
	private OffsetDateTime lastReviewedAt;

	@Column(name = "review_count", nullable = false)
	private Integer reviewCount;

	@Column(name = "correct_count", nullable = false)
	private Integer correctCount;

	@Column(name = "incorrect_count", nullable = false)
	private Integer incorrectCount;

	protected CardProgress() {
	}

	public CardProgress(User user, Card card) {
		this.user = user;
		this.card = card;
		this.easeFactor = new BigDecimal("2.5");
		this.intervalDays = 0;
		this.repetitions = 0;
		this.dueDate = LocalDate.now();
		this.reviewCount = 0;
		this.correctCount = 0;
		this.incorrectCount = 0;
	}

	public Long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public Card getCard() {
		return card;
	}

	public BigDecimal getEaseFactor() {
		return easeFactor;
	}

	public void setEaseFactor(BigDecimal easeFactor) {
		this.easeFactor = easeFactor;
	}

	public Integer getIntervalDays() {
		return intervalDays;
	}

	public void setIntervalDays(Integer intervalDays) {
		this.intervalDays = intervalDays;
	}

	public Integer getRepetitions() {
		return repetitions;
	}

	public void setRepetitions(Integer repetitions) {
		this.repetitions = repetitions;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public OffsetDateTime getLastReviewedAt() {
		return lastReviewedAt;
	}

	public void setLastReviewedAt(OffsetDateTime lastReviewedAt) {
		this.lastReviewedAt = lastReviewedAt;
	}

	public Integer getReviewCount() {
		return reviewCount;
	}

	public void setReviewCount(Integer reviewCount) {
		this.reviewCount = reviewCount;
	}

	public Integer getCorrectCount() {
		return correctCount;
	}

	public void setCorrectCount(Integer correctCount) {
		this.correctCount = correctCount;
	}

	public Integer getIncorrectCount() {
		return incorrectCount;
	}

	public void setIncorrectCount(Integer incorrectCount) {
		this.incorrectCount = incorrectCount;
	}
}
