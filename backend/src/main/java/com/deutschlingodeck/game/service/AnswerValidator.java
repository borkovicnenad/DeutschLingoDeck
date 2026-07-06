package com.deutschlingodeck.game.service;

import com.deutschlingodeck.dictionary.dto.CardType;
import com.deutschlingodeck.dictionary.entity.Card;
import com.deutschlingodeck.game.dto.ValidationResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Classifies a typed answer against a card's accepted answers. This is a heuristic MVP
 * classifier (exact match, article-only mismatch, small edit-distance typo, word-count
 * mismatch, substring partial match) - not a linguistically complete grader, but consistent
 * and good enough to drive spaced-repetition scheduling and user feedback.
 */
@Component
public class AnswerValidator {

	private static final Set<String> GERMAN_ARTICLES = Set.of("der", "die", "das", "den", "dem", "des");

	public ValidationResult validate(Card card, String givenAnswer) {
		String normalizedGiven = normalize(givenAnswer);
		List<String> accepted = card.getAcceptedAnswers();
		if (accepted == null || accepted.isEmpty()) {
			String translation = card.getPrimaryTranslation();
			return translation != null && normalize(translation).equals(normalizedGiven)
					? ValidationResult.CORRECT
					: ValidationResult.WRONG_TRANSLATION;
		}

		for (String candidate : accepted) {
			if (normalize(candidate).equals(normalizedGiven)) {
				return ValidationResult.CORRECT;
			}
		}

		for (String candidate : accepted) {
			String[] candidateParts = splitArticleAndWord(candidate);
			String[] givenParts = splitArticleAndWord(givenAnswer);
			boolean sameWord = !candidateParts[1].isEmpty() && candidateParts[1].equals(givenParts[1]);
			boolean differentArticle = !candidateParts[0].isEmpty() && !candidateParts[0].equals(givenParts[0]);
			if (sameWord && differentArticle) {
				return ValidationResult.WRONG_ARTICLE;
			}
		}

		for (String candidate : accepted) {
			String normalizedCandidate = normalize(candidate);
			int distance = levenshtein(normalizedCandidate, normalizedGiven);
			int threshold = Math.max(1, normalizedCandidate.length() / 5);
			if (distance > 0 && distance <= threshold) {
				return ValidationResult.TYPO;
			}
		}

		for (String candidate : accepted) {
			String normalizedCandidate = normalize(candidate);
			if (!normalizedGiven.isEmpty()
					&& (normalizedCandidate.contains(normalizedGiven) || normalizedGiven.contains(normalizedCandidate))) {
				return card.getCardType() == CardType.SENTENCE ? ValidationResult.WRONG_SENTENCE : ValidationResult.PARTIALLY_CORRECT;
			}
		}

		int givenWordCount = normalizedGiven.isEmpty() ? 0 : normalizedGiven.split("\\s+").length;
		int candidateWordCount = normalize(accepted.get(0)).split("\\s+").length;
		if (givenWordCount < candidateWordCount) {
			return ValidationResult.MISSING_WORD;
		}
		if (givenWordCount > candidateWordCount) {
			return ValidationResult.EXTRA_WORD;
		}
		return ValidationResult.WRONG_TRANSLATION;
	}

	private String normalize(String value) {
		return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
	}

	/** Returns {@code [article, word]}; {@code article} is empty when none is present. */
	private String[] splitArticleAndWord(String value) {
		String normalized = normalize(value);
		String[] parts = normalized.split(" ", 2);
		if (parts.length == 2 && GERMAN_ARTICLES.contains(parts[0])) {
			return new String[] {parts[0], parts[1]};
		}
		return new String[] {"", normalized};
	}

	private int levenshtein(String a, String b) {
		int[][] distance = new int[a.length() + 1][b.length() + 1];
		for (int i = 0; i <= a.length(); i++) {
			distance[i][0] = i;
		}
		for (int j = 0; j <= b.length(); j++) {
			distance[0][j] = j;
		}
		for (int i = 1; i <= a.length(); i++) {
			for (int j = 1; j <= b.length(); j++) {
				int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
				distance[i][j] = Math.min(Math.min(distance[i - 1][j] + 1, distance[i][j - 1] + 1), distance[i - 1][j - 1] + cost);
			}
		}
		return distance[a.length()][b.length()];
	}
}
