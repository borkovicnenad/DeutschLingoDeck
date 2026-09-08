package com.deutschlingodeck.statistics.dto;

/**
 * An exhaustive, non-overlapping partition of the user's cards (optionally scoped to one
 * dictionary) by retention, derived from {@code CardProgress.intervalDays} - the one signal that
 * already only grows on a correct answer and resets on a wrong one, so the tiers reflect actual
 * recall performance rather than an arbitrary attempt count. Every card falls into exactly one
 * tier, so the five counts always sum to {@code totalCards}.
 */
public record MasteryBreakdownResponse(
		Integer newCount,
		Integer learningCount,
		Integer familiarCount,
		Integer strongCount,
		Integer masteredCount,
		Integer totalCards
) {
}
