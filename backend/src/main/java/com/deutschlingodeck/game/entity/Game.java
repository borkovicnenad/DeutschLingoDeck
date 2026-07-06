package com.deutschlingodeck.game.entity;

import com.deutschlingodeck.authentication.entity.User;
import com.deutschlingodeck.dictionary.entity.Dictionary;
import com.deutschlingodeck.game.dto.GameStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "games")
public class Game {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dictionary_id", nullable = false)
	private Dictionary dictionary;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private GameStatus status;

	@Column(nullable = false)
	private Integer totalCards;

	private Integer answeredCards;

	private Integer correctAnswers;

	private Integer incorrectAnswers;

	@Column(nullable = false)
	private OffsetDateTime startedAt;

	private OffsetDateTime finishedAt;

	/**
	 * Comma-separated, ordered card ids dealt for this game at creation time.
	 * {@code answeredCards} indexes into this list to find the current card,
	 * since due/new/resting cards are only decided once, at deal time.
	 */
	@Column(name = "deck_card_ids")
	private String deckCardIds;

	protected Game() {
	}

	public Game(User user, Dictionary dictionary, GameStatus status, Integer totalCards) {
		this.user = user;
		this.dictionary = dictionary;
		this.status = status;
		this.totalCards = totalCards;
	}

	public Long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Dictionary getDictionary() {
		return dictionary;
	}

	public void setDictionary(Dictionary dictionary) {
		this.dictionary = dictionary;
	}

	public GameStatus getStatus() {
		return status;
	}

	public void setStatus(GameStatus status) {
		this.status = status;
	}

	public Integer getTotalCards() {
		return totalCards;
	}

	public void setTotalCards(Integer totalCards) {
		this.totalCards = totalCards;
	}

	public Integer getAnsweredCards() {
		return answeredCards;
	}

	public void setAnsweredCards(Integer answeredCards) {
		this.answeredCards = answeredCards;
	}

	public Integer getCorrectAnswers() {
		return correctAnswers;
	}

	public void setCorrectAnswers(Integer correctAnswers) {
		this.correctAnswers = correctAnswers;
	}

	public Integer getIncorrectAnswers() {
		return incorrectAnswers;
	}

	public void setIncorrectAnswers(Integer incorrectAnswers) {
		this.incorrectAnswers = incorrectAnswers;
	}

	public OffsetDateTime getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(OffsetDateTime startedAt) {
		this.startedAt = startedAt;
	}

	public OffsetDateTime getFinishedAt() {
		return finishedAt;
	}

	public void setFinishedAt(OffsetDateTime finishedAt) {
		this.finishedAt = finishedAt;
	}

	public List<Long> getDeckCardIds() {
		if (deckCardIds == null || deckCardIds.isBlank()) {
			return List.of();
		}
		return Arrays.stream(deckCardIds.split(",")).map(Long::parseLong).toList();
	}

	public void setDeckCardIds(List<Long> cardIds) {
		this.deckCardIds = cardIds.stream().map(String::valueOf).collect(Collectors.joining(","));
	}
}
