package com.deutschlingodeck.game.dto;

import jakarta.validation.constraints.NotNull;

public record CreateGameRequest(
		@NotNull Long dictionaryId,
		String gameMode
) {
}
