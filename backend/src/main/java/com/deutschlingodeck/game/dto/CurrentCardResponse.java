package com.deutschlingodeck.game.dto;

import com.deutschlingodeck.dictionary.dto.CardType;

public record CurrentCardResponse(
		Long id,
		String sourceText,
		CardType cardType,
		String article,
		String example
) {
}
