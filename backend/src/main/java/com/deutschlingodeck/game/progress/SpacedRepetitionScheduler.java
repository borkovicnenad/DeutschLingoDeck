package com.deutschlingodeck.game.progress;

import com.deutschlingodeck.game.dto.ValidationResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

/**
 * SM-2-lite spaced-repetition scheduler. The typed-answer quiz only yields a
 * {@link ValidationResult}, not an explicit again/hard/good/easy rating, so results are
 * bucketed into a 3-level quality ({@link Quality}) before scheduling — there is no "easy"
 * signal available from answer validation alone. This is a deliberate MVP simplification:
 * consistent and testable, not a byte-for-byte SM-2 implementation.
 */
@Component
public class SpacedRepetitionScheduler {

	private static final BigDecimal MIN_EASE_FACTOR = new BigDecimal("1.3");
	private static final BigDecimal GOOD_EASE_DELTA = new BigDecimal("0.05");
	private static final BigDecimal HARD_EASE_DELTA = new BigDecimal("-0.15");

	public enum Quality {
		AGAIN,
		HARD,
		GOOD
	}

	public static Quality qualityFor(ValidationResult result) {
		return switch (result) {
			case CORRECT -> Quality.GOOD;
			case PARTIALLY_CORRECT, TYPO -> Quality.HARD;
			default -> Quality.AGAIN;
		};
	}

	/** Mutates {@code progress} in place to reflect the outcome of this review. */
	public void schedule(CardProgress progress, ValidationResult result, OffsetDateTime reviewedAt) {
		Quality quality = qualityFor(result);
		boolean correct = quality != Quality.AGAIN;

		progress.setReviewCount(progress.getReviewCount() + 1);
		if (correct) {
			progress.setCorrectCount(progress.getCorrectCount() + 1);
		} else {
			progress.setIncorrectCount(progress.getIncorrectCount() + 1);
		}

		if (quality == Quality.AGAIN) {
			progress.setRepetitions(0);
			progress.setIntervalDays(1);
		} else {
			int repetitions = progress.getRepetitions() + 1;
			progress.setRepetitions(repetitions);
			progress.setIntervalDays(nextInterval(repetitions, progress));
			progress.setEaseFactor(nextEaseFactor(progress.getEaseFactor(), quality));
		}

		progress.setLastReviewedAt(reviewedAt);
		progress.setDueDate(reviewedAt.toLocalDate().plusDays(progress.getIntervalDays()));
	}

	private int nextInterval(int repetitions, CardProgress progress) {
		if (repetitions == 1) {
			return 1;
		}
		if (repetitions == 2) {
			return 6;
		}
		double scaled = progress.getIntervalDays() * progress.getEaseFactor().doubleValue();
		return Math.max(1, (int) Math.round(scaled));
	}

	private BigDecimal nextEaseFactor(BigDecimal currentEaseFactor, Quality quality) {
		BigDecimal delta = quality == Quality.HARD ? HARD_EASE_DELTA : GOOD_EASE_DELTA;
		BigDecimal updated = currentEaseFactor.add(delta).setScale(2, RoundingMode.HALF_UP);
		return updated.compareTo(MIN_EASE_FACTOR) < 0 ? MIN_EASE_FACTOR : updated;
	}
}
