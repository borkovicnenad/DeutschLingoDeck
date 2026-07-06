package com.deutschlingodeck.game.repository;

import java.time.OffsetDateTime;

/** Per-card answer aggregate for the current user, produced by {@link GameAnswerRepository#aggregateForCards}. */
public record CardAnswerAggregate(
		Long cardId,
		Long timesShown,
		Long timesCorrect,
		OffsetDateTime lastShownAt,
		OffsetDateTime lastCorrectAt
) {
}
