package com.deutschlingodeck.game.engine;

import com.deutschlingodeck.game.entity.Game;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements in-session repetition of missed cards, so a wrong answer resurfaces again later in
 * the same game instead of only affecting the next game via {@code CardProgress} due dates.
 * <p>
 * {@link Game#getDeckCardIds()} is an ordered, mutable list that {@link Game#getAnsweredCards()}
 * indexes into to find the next card. On a wrong answer this policy appends the same card id
 * again a few positions ahead (never immediately next) and grows {@code totalCards} to match, so
 * the progress bar honestly reflects the extra review rather than silently dropping it. Correct
 * answers are left alone - they simply never get a second entry, so they gradually disappear from
 * the remaining queue. Each additional miss of the same card shortens the gap before it comes
 * back, so repeatedly-wrong cards are reviewed more frequently within the session.
 */
@Component
public class RequeuePolicy {

	private static final int BASE_GAP = 6;
	private static final int GAP_SHRINK_PER_MISS = 2;
	private static final int MIN_GAP = 2;

	/** Mutates {@code game} in place. {@code answeredIndex} is the position of {@code cardId} that was just answered. */
	public void onAnswer(Game game, Long cardId, boolean correct, int answeredIndex) {
		if (correct) {
			return;
		}

		List<Long> deck = new ArrayList<>(game.getDeckCardIds());
		long priorOccurrences = deck.stream().filter(cardId::equals).count();
		int gap = Math.max(MIN_GAP, BASE_GAP - (int) (priorOccurrences - 1) * GAP_SHRINK_PER_MISS);
		int insertAt = Math.min(deck.size(), answeredIndex + 1 + gap);

		deck.add(insertAt, cardId);
		game.setDeckCardIds(deck);
		game.setTotalCards(deck.size());
	}
}
