package com.deutschlingodeck.game.engine;

import com.deutschlingodeck.dictionary.entity.Card;
import com.deutschlingodeck.dictionary.repository.CardRepository;
import com.deutschlingodeck.game.progress.CardProgress;
import com.deutschlingodeck.game.progress.CardProgressRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

/**
 * Builds the ordered card deck for a new game session using weighted-random selection instead
 * of a fixed position order, so that:
 * <ul>
 *   <li>consecutive games on the same dictionary produce different orders and different cards
 *       (weighted sampling, not a deterministic sort);</li>
 *   <li>cards due for spaced-repetition review are dealt first, cards never seen before are dealt
 *       next, and cards not yet due are dealt last, mirroring the previous tiering;</li>
 *   <li>within each tier, cards with a lower historical success rate and more overdue reviews are
 *       proportionally more likely to be picked, while cards long confirmed as mastered
 *       ({@link CardProgress#MASTERED_INTERVAL_DAYS_THRESHOLD}+ day interval) are heavily
 *       dampened so they rarely resurface.</li>
 * </ul>
 * The {@link Random} is injected so tests can supply a seeded instance for deterministic
 * assertions, while production uses a real one.
 */
@Component
public class DeckBuilder {

	/** Cap on how many cards a single game deals, even if more are due. */
	public static final int SESSION_CARD_LIMIT = 20;

	private static final double BASE_WEIGHT = 1.0;
	private static final double MISTAKE_WEIGHT_SCALE = 4.0;
	private static final double OVERDUE_WEIGHT_PER_DAY = 0.15;
	private static final double OVERDUE_WEIGHT_CAP = 3.0;
	private static final double MASTERED_DAMPENER = 0.15;
	private static final double MIN_WEIGHT = 0.01;
	private static final double NEW_CARD_WEIGHT = 1.0;

	private final CardRepository cardRepository;
	private final CardProgressRepository cardProgressRepository;
	private final Random random;

	public DeckBuilder(CardRepository cardRepository, CardProgressRepository cardProgressRepository, Random random) {
		this.cardRepository = cardRepository;
		this.cardProgressRepository = cardProgressRepository;
		this.random = random;
	}

	public List<Card> buildDeck(Long userId, Long dictionaryId) {
		List<Card> allCards = cardRepository.findByDictionaryIdOrderByPosition(dictionaryId);
		if (allCards.isEmpty()) {
			return List.of();
		}

		Map<Long, CardProgress> progressByCardId = cardProgressRepository.findAllForDictionary(userId, dictionaryId)
				.stream()
				.collect(Collectors.toMap(progress -> progress.getCard().getId(), Function.identity()));

		LocalDate today = LocalDate.now();
		List<Card> due = new ArrayList<>();
		List<Card> fresh = new ArrayList<>();
		List<Card> resting = new ArrayList<>();
		for (Card card : allCards) {
			CardProgress progress = progressByCardId.get(card.getId());
			if (progress == null) {
				fresh.add(card);
			} else if (!progress.getDueDate().isAfter(today)) {
				due.add(card);
			} else {
				resting.add(card);
			}
		}

		int cap = Math.min(allCards.size(), SESSION_CARD_LIMIT);
		List<Card> deck = new ArrayList<>();
		deck.addAll(weightedSample(due, card -> dueWeight(progressByCardId.get(card.getId()), today), cap - deck.size()));
		deck.addAll(weightedSample(fresh, card -> NEW_CARD_WEIGHT, cap - deck.size()));
		deck.addAll(weightedSample(resting, card -> restingWeight(progressByCardId.get(card.getId())), cap - deck.size()));
		return deck;
	}

	private double dueWeight(CardProgress progress, LocalDate today) {
		long overdueDays = Math.max(0, ChronoUnit.DAYS.between(progress.getDueDate(), today));
		double overdueBoost = Math.min(OVERDUE_WEIGHT_CAP, overdueDays * OVERDUE_WEIGHT_PER_DAY);
		return mistakeWeight(progress) + overdueBoost;
	}

	private double restingWeight(CardProgress progress) {
		double weight = mistakeWeight(progress);
		Integer intervalDays = progress.getIntervalDays();
		if (intervalDays != null && intervalDays >= CardProgress.MASTERED_INTERVAL_DAYS_THRESHOLD) {
			weight *= MASTERED_DAMPENER;
		}
		return weight;
	}

	/** Higher for cards the user gets wrong more often; a never-reviewed card gets the neutral base weight. */
	private double mistakeWeight(CardProgress progress) {
		int reviewCount = progress.getReviewCount() == null ? 0 : progress.getReviewCount();
		if (reviewCount == 0) {
			return BASE_WEIGHT;
		}
		int correctCount = progress.getCorrectCount() == null ? 0 : progress.getCorrectCount();
		double successRate = (double) correctCount / reviewCount;
		return BASE_WEIGHT + (1.0 - successRate) * MISTAKE_WEIGHT_SCALE;
	}

	/**
	 * Weighted sampling without replacement: repeatedly draws a random point in
	 * {@code [0, totalWeight)}, walks the cumulative weights to find which candidate's "bucket"
	 * contains it, removes that candidate, and re-draws. This keeps every candidate reachable
	 * (true randomness, avoiding "same few cards every time") while making higher-weight
	 * candidates proportionally more likely to come up first.
	 */
	private List<Card> weightedSample(List<Card> candidates, ToDoubleFunction<Card> weightFn, int count) {
		if (count <= 0 || candidates.isEmpty()) {
			return List.of();
		}
		List<Card> pool = new ArrayList<>(candidates);
		List<Double> weights = new ArrayList<>(pool.size());
		for (Card card : pool) {
			weights.add(Math.max(MIN_WEIGHT, weightFn.applyAsDouble(card)));
		}

		int target = Math.min(count, pool.size());
		List<Card> picked = new ArrayList<>(target);
		for (int i = 0; i < target; i++) {
			double total = weights.stream().mapToDouble(Double::doubleValue).sum();
			double draw = random.nextDouble() * total;
			double cumulative = 0;
			int chosenIndex = pool.size() - 1;
			for (int j = 0; j < pool.size(); j++) {
				cumulative += weights.get(j);
				if (draw < cumulative) {
					chosenIndex = j;
					break;
				}
			}
			picked.add(pool.remove(chosenIndex));
			weights.remove(chosenIndex);
		}
		return picked;
	}
}
