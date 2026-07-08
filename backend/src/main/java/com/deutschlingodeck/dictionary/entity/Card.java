package com.deutschlingodeck.dictionary.entity;

import com.deutschlingodeck.dictionary.dto.CardType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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

import java.util.List;

@Entity
@Table(name = "cards")
public class Card {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dictionary_id", nullable = false)
	private Dictionary dictionary;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CardType cardType;

	private String article;

	@Column(nullable = false)
	private String sourceText;

	private String primaryTranslation;

	@ElementCollection
	@CollectionTable(name = "card_accepted_answers", joinColumns = @JoinColumn(name = "card_id"))
	@Column(name = "answer")
	private List<String> acceptedAnswers;

	@ElementCollection
	@CollectionTable(name = "card_tags", joinColumns = @JoinColumn(name = "card_id"))
	@Column(name = "tag")
	private List<String> tags;

	private String example;

	private String grammarInfo;

	private Integer difficultyLevel;

	@Column(nullable = false)
	private Integer position;

	protected Card() {
	}

	public Card(Dictionary dictionary, CardType cardType, String sourceText, Integer position) {
		this.dictionary = dictionary;
		this.cardType = cardType;
		this.sourceText = sourceText;
		this.position = position;
	}

	public Long getId() {
		return id;
	}

	public Dictionary getDictionary() {
		return dictionary;
	}

	public void setDictionary(Dictionary dictionary) {
		this.dictionary = dictionary;
	}

	public CardType getCardType() {
		return cardType;
	}

	public void setCardType(CardType cardType) {
		this.cardType = cardType;
	}

	public String getArticle() {
		return article;
	}

	public void setArticle(String article) {
		this.article = article;
	}

	public String getSourceText() {
		return sourceText;
	}

	public void setSourceText(String sourceText) {
		this.sourceText = sourceText;
	}

	public String getPrimaryTranslation() {
		return primaryTranslation;
	}

	public void setPrimaryTranslation(String primaryTranslation) {
		this.primaryTranslation = primaryTranslation;
	}

	public List<String> getAcceptedAnswers() {
		return acceptedAnswers;
	}

	public void setAcceptedAnswers(List<String> acceptedAnswers) {
		this.acceptedAnswers = acceptedAnswers;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getExample() {
		return example;
	}

	public void setExample(String example) {
		this.example = example;
	}

	public String getGrammarInfo() {
		return grammarInfo;
	}

	public void setGrammarInfo(String grammarInfo) {
		this.grammarInfo = grammarInfo;
	}

	public Integer getDifficultyLevel() {
		return difficultyLevel;
	}

	public void setDifficultyLevel(Integer difficultyLevel) {
		this.difficultyLevel = difficultyLevel;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}
}
