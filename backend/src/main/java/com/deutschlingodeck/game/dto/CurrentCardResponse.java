package com.deutschlingodeck.game.dto;

import com.deutschlingodeck.dictionary.dto.CardType;

/**
 * The question side of a card, redacted according to the dictionary's learning direction:
 * whichever field is the "answer" for this direction (the German word for a recall-German
 * dictionary, the translation otherwise) is {@code null} until the answer is submitted.
 */
public record CurrentCardResponse(
		Long id,
		CardType cardType,
		String article,
		String sourceText,
		String primaryTranslation,
		String example,
		String grammarInfo
) {
}
