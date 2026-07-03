package com.deutschlingodeck.game.entity;

import com.deutschlingodeck.dictionary.entity.Card;
import com.deutschlingodeck.game.dto.ValidationResult;
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

@Entity
@Table(name = "game_answers")
public class GameAnswer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "game_id", nullable = false)
	private Game game;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "card_id", nullable = false)
	private Card card;

	@Column(nullable = false)
	private String givenAnswer;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ValidationResult result;

	@Column(nullable = false)
	private boolean correct;

	@Column(nullable = false)
	private Long responseTimeMs;

	@Column(nullable = false)
	private OffsetDateTime answeredAt;

	protected GameAnswer() {
	}

	public GameAnswer(Game game, Card card, String givenAnswer, ValidationResult result, boolean correct, Long responseTimeMs) {
		this.game = game;
		this.card = card;
		this.givenAnswer = givenAnswer;
		this.result = result;
		this.correct = correct;
		this.responseTimeMs = responseTimeMs;
	}

	public Long getId() {
		return id;
	}

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public Card getCard() {
		return card;
	}

	public void setCard(Card card) {
		this.card = card;
	}

	public String getGivenAnswer() {
		return givenAnswer;
	}

	public void setGivenAnswer(String givenAnswer) {
		this.givenAnswer = givenAnswer;
	}

	public ValidationResult getResult() {
		return result;
	}

	public void setResult(ValidationResult result) {
		this.result = result;
	}

	public boolean isCorrect() {
		return correct;
	}

	public void setCorrect(boolean correct) {
		this.correct = correct;
	}

	public Long getResponseTimeMs() {
		return responseTimeMs;
	}

	public void setResponseTimeMs(Long responseTimeMs) {
		this.responseTimeMs = responseTimeMs;
	}

	public OffsetDateTime getAnsweredAt() {
		return answeredAt;
	}

	public void setAnsweredAt(OffsetDateTime answeredAt) {
		this.answeredAt = answeredAt;
	}
}
