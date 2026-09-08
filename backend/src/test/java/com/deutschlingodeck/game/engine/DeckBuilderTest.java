package com.deutschlingodeck.game.engine;

import com.deutschlingodeck.authentication.entity.User;
import com.deutschlingodeck.dictionary.dto.CardType;
import com.deutschlingodeck.dictionary.entity.Card;
import com.deutschlingodeck.dictionary.entity.Dictionary;
import com.deutschlingodeck.dictionary.repository.CardRepository;
import com.deutschlingodeck.game.progress.CardProgress;
import com.deutschlingodeck.game.progress.CardProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeckBuilderTest {

	@Mock
	private CardRepository cardRepository;
	@Mock
	private CardProgressRepository cardProgressRepository;

	private static final Long USER_ID = 1L;
	private static final Long DICTIONARY_ID = 9L;
	private User owner;
	private Dictionary dictionary;

	@BeforeEach
	void setUp() {
		owner = new User("owner@example.com", "hash", "Owner");
		ReflectionTestUtils.setField(owner, "id", USER_ID);
		dictionary = new Dictionary(owner, "Travel", "de", "hr");
		ReflectionTestUtils.setField(dictionary, "id", DICTIONARY_ID);
	}

	@Test
	void allNewCards_dealsEveryCardWhenUnderTheSessionLimit() {
		List<Card> cards = cards(5);
		when(cardRepository.findByDictionaryIdOrderByPosition(DICTIONARY_ID)).thenReturn(cards);
		when(cardProgressRepository.findAllForDictionary(USER_ID, DICTIONARY_ID)).thenReturn(List.of());

		DeckBuilder deckBuilder = new DeckBuilder(cardRepository, cardProgressRepository, new Random(1));
		List<Card> deck = deckBuilder.buildDeck(USER_ID, DICTIONARY_ID);

		assertThat(deck).hasSize(5);
		assertThat(idsOf(deck)).isEqualTo(idsOf(cards));
	}

	@Test
	void dueCards_areExhaustedBeforeNeverAnsweredCardsWhenOverTheSessionLimit() {
		List<Card> dueCards = cards(25);
		List<Card> freshCards = cards(5, 100);
		List<Card> allCards = new ArrayList<>(dueCards);
		allCards.addAll(freshCards);

		List<CardProgress> dueProgress = dueCards.stream().map(card -> new CardProgress(owner, card)).toList();

		when(cardRepository.findByDictionaryIdOrderByPosition(DICTIONARY_ID)).thenReturn(allCards);
		when(cardProgressRepository.findAllForDictionary(USER_ID, DICTIONARY_ID)).thenReturn(dueProgress);

		DeckBuilder deckBuilder = new DeckBuilder(cardRepository, cardProgressRepository, new Random(2));
		List<Card> deck = deckBuilder.buildDeck(USER_ID, DICTIONARY_ID);

		assertThat(deck).hasSize(DeckBuilder.SESSION_CARD_LIMIT);
		assertThat(idsOf(deck)).isSubsetOf(idsOf(dueCards));
	}

	@Test
	void sameSeed_producesTheSameDeckOrder() {
		List<Card> cards = cards(10);
		when(cardRepository.findByDictionaryIdOrderByPosition(DICTIONARY_ID)).thenReturn(cards);
		when(cardProgressRepository.findAllForDictionary(USER_ID, DICTIONARY_ID)).thenReturn(List.of());

		List<Card> first = new DeckBuilder(cardRepository, cardProgressRepository, new Random(42))
				.buildDeck(USER_ID, DICTIONARY_ID);
		List<Card> second = new DeckBuilder(cardRepository, cardProgressRepository, new Random(42))
				.buildDeck(USER_ID, DICTIONARY_ID);

		assertThat(idOrder(first)).isEqualTo(idOrder(second));
	}

	private List<Card> cards(int count) {
		return cards(count, 0);
	}

	private List<Card> cards(int count, int idOffset) {
		List<Card> cards = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			Card card = new Card(dictionary, CardType.WORD, "word-" + (idOffset + i), i);
			ReflectionTestUtils.setField(card, "id", (long) (idOffset + i + 1));
			cards.add(card);
		}
		return cards;
	}

	private Set<Long> idsOf(List<Card> cards) {
		return cards.stream().map(Card::getId).collect(Collectors.toSet());
	}

	private List<Long> idOrder(List<Card> cards) {
		return cards.stream().map(Card::getId).toList();
	}
}
