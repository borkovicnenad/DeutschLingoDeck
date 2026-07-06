package com.deutschlingodeck.game.progress;

import com.deutschlingodeck.game.dto.ValidationResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class SpacedRepetitionSchedulerTest {

	private static final OffsetDateTime NOW = OffsetDateTime.of(2026, 6, 1, 10, 0, 0, 0, ZoneOffset.UTC);

	private final SpacedRepetitionScheduler scheduler = new SpacedRepetitionScheduler();

	@Test
	void again_resetsRepetitionsAndSchedulesForTomorrow() {
		CardProgress progress = new CardProgress(null, null);
		progress.setRepetitions(4);
		progress.setIntervalDays(20);

		scheduler.schedule(progress, ValidationResult.WRONG_TRANSLATION, NOW);

		assertThat(progress.getRepetitions()).isZero();
		assertThat(progress.getIntervalDays()).isEqualTo(1);
		assertThat(progress.getDueDate()).isEqualTo(NOW.toLocalDate().plusDays(1));
		assertThat(progress.getReviewCount()).isEqualTo(1);
		assertThat(progress.getIncorrectCount()).isEqualTo(1);
		assertThat(progress.getCorrectCount()).isZero();
		assertThat(progress.getEaseFactor()).isEqualByComparingTo("2.50");
	}

	@Test
	void good_growsIntervalAcrossRepetitionsAndRaisesEaseFactor() {
		CardProgress progress = new CardProgress(null, null);

		scheduler.schedule(progress, ValidationResult.CORRECT, NOW);
		assertThat(progress.getRepetitions()).isEqualTo(1);
		assertThat(progress.getIntervalDays()).isEqualTo(1);
		assertThat(progress.getEaseFactor()).isEqualByComparingTo("2.55");

		scheduler.schedule(progress, ValidationResult.CORRECT, NOW);
		assertThat(progress.getRepetitions()).isEqualTo(2);
		assertThat(progress.getIntervalDays()).isEqualTo(6);
		assertThat(progress.getEaseFactor()).isEqualByComparingTo("2.60");

		scheduler.schedule(progress, ValidationResult.CORRECT, NOW);
		assertThat(progress.getRepetitions()).isEqualTo(3);
		assertThat(progress.getIntervalDays()).isEqualTo(16); // round(6 * 2.60)
		assertThat(progress.getEaseFactor()).isEqualByComparingTo("2.65");
		assertThat(progress.getDueDate()).isEqualTo(NOW.toLocalDate().plusDays(16));
		assertThat(progress.getCorrectCount()).isEqualTo(3);
	}

	@Test
	void hard_stillAdvancesButLowersEaseFactor() {
		CardProgress progress = new CardProgress(null, null);

		scheduler.schedule(progress, ValidationResult.TYPO, NOW);

		assertThat(progress.getRepetitions()).isEqualTo(1);
		assertThat(progress.getIntervalDays()).isEqualTo(1);
		assertThat(progress.getEaseFactor()).isEqualByComparingTo("2.35");
		assertThat(progress.getCorrectCount()).isEqualTo(1);
	}

	@Test
	void easeFactor_neverDropsBelowMinimum() {
		CardProgress progress = new CardProgress(null, null);
		progress.setEaseFactor(new BigDecimal("1.35"));

		scheduler.schedule(progress, ValidationResult.PARTIALLY_CORRECT, NOW);

		assertThat(progress.getEaseFactor()).isEqualByComparingTo("1.30");
	}
}
