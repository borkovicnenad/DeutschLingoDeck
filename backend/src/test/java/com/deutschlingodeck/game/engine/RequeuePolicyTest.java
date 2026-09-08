package com.deutschlingodeck.game.engine;

import com.deutschlingodeck.authentication.entity.User;
import com.deutschlingodeck.dictionary.entity.Dictionary;
import com.deutschlingodeck.game.dto.GameStatus;
import com.deutschlingodeck.game.entity.Game;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

class RequeuePolicyTest {

	private final RequeuePolicy policy = new RequeuePolicy();

	@Test
	void correctAnswer_leavesTheDeckUntouched() {
		Game game = gameWithDeck(1L, 2L, 3L);

		policy.onAnswer(game, 2L, true, 1);

		assertThat(game.getDeckCardIds()).containsExactly(1L, 2L, 3L);
		assertThat(game.getTotalCards()).isEqualTo(3);
	}

	@Test
	void firstMiss_reinsertsSixCardsLater_neverImmediatelyNext() {
		Game game = gameWithDeck(range(1, 15)); // ids 1..15
		int answeredIndex = 1; // card id 2

		policy.onAnswer(game, 2L, false, answeredIndex);

		List<Long> deck = game.getDeckCardIds();
		int reinsertedAt = indexOfFrom(deck, 2L, answeredIndex + 1);
		assertThat(reinsertedAt).isEqualTo(answeredIndex + 1 + 6);
		assertThat(reinsertedAt).isGreaterThan(answeredIndex + 1);
		assertThat(game.getTotalCards()).isEqualTo(16);
	}

	@Test
	void repeatedMisses_shrinkTheGapDownToTheMinimum() {
		Game game = gameWithDeck(range(1, 30));

		policy.onAnswer(game, 5L, false, 4); // 1st miss: gap 6
		int firstReinsert = indexOfFrom(game.getDeckCardIds(), 5L, 5);

		policy.onAnswer(game, 5L, false, firstReinsert); // 2nd miss: gap 4
		int secondReinsert = indexOfFrom(game.getDeckCardIds(), 5L, firstReinsert + 1);
		assertThat(secondReinsert - firstReinsert).isEqualTo(1 + 4);

		policy.onAnswer(game, 5L, false, secondReinsert); // 3rd miss: gap 2
		int thirdReinsert = indexOfFrom(game.getDeckCardIds(), 5L, secondReinsert + 1);
		assertThat(thirdReinsert - secondReinsert).isEqualTo(1 + 2);

		policy.onAnswer(game, 5L, false, thirdReinsert); // 4th miss: floors at minimum gap 2
		int fourthReinsert = indexOfFrom(game.getDeckCardIds(), 5L, thirdReinsert + 1);
		assertThat(fourthReinsert - thirdReinsert).isEqualTo(1 + 2);
	}

	private int indexOfFrom(List<Long> list, long value, int fromIndex) {
		for (int i = fromIndex; i < list.size(); i++) {
			if (list.get(i) == value) {
				return i;
			}
		}
		throw new AssertionError("Value " + value + " not found from index " + fromIndex);
	}

	private Game gameWithDeck(Long... cardIds) {
		User owner = new User("owner@example.com", "hash", "Owner");
		Dictionary dictionary = new Dictionary(owner, "Travel", "de", "hr");
		Game game = new Game(owner, dictionary, GameStatus.IN_PROGRESS, cardIds.length);
		game.setDeckCardIds(List.of(cardIds));
		return game;
	}

	private Long[] range(int startInclusive, int endInclusive) {
		return LongStream.rangeClosed(startInclusive, endInclusive).boxed().toArray(Long[]::new);
	}
}
