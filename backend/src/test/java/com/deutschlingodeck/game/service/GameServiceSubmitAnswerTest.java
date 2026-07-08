package com.deutschlingodeck.game.service;

import com.deutschlingodeck.authentication.entity.User;
import com.deutschlingodeck.authentication.repository.UserRepository;
import com.deutschlingodeck.common.security.CurrentUserProvider;
import com.deutschlingodeck.common.security.OwnershipGuard;
import com.deutschlingodeck.dictionary.dto.CardType;
import com.deutschlingodeck.dictionary.entity.Card;
import com.deutschlingodeck.dictionary.entity.Dictionary;
import com.deutschlingodeck.dictionary.repository.CardRepository;
import com.deutschlingodeck.dictionary.repository.DictionaryRepository;
import com.deutschlingodeck.game.dto.AnswerValidationResponse;
import com.deutschlingodeck.game.dto.CardRevealResponse;
import com.deutschlingodeck.game.dto.GameStatus;
import com.deutschlingodeck.game.dto.SubmitAnswerRequest;
import com.deutschlingodeck.game.dto.ValidationResult;
import com.deutschlingodeck.game.entity.Game;
import com.deutschlingodeck.game.mapper.GameMapper;
import com.deutschlingodeck.game.progress.CardProgressRepository;
import com.deutschlingodeck.game.progress.SpacedRepetitionScheduler;
import com.deutschlingodeck.game.repository.GameAnswerRepository;
import com.deutschlingodeck.game.repository.GameRepository;
import com.deutschlingodeck.gamification.service.GamificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/** Skip (the frontend's "give up on this card" action) submits {@code ""} as the answer. */
@ExtendWith(MockitoExtension.class)
class GameServiceSubmitAnswerTest {

	@Mock
	private GameRepository gameRepository;
	@Mock
	private GameAnswerRepository gameAnswerRepository;
	@Mock
	private DictionaryRepository dictionaryRepository;
	@Mock
	private CardRepository cardRepository;
	@Mock
	private CardProgressRepository cardProgressRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private GameMapper gameMapper;
	@Mock
	private AnswerValidator answerValidator;
	@Mock
	private SpacedRepetitionScheduler scheduler;
	@Mock
	private GamificationService gamificationService;
	@Mock
	private CurrentUserProvider currentUserProvider;

	private GameServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new GameServiceImpl(
				gameRepository, gameAnswerRepository, dictionaryRepository, cardRepository, cardProgressRepository,
				userRepository, gameMapper, answerValidator, scheduler, gamificationService, currentUserProvider,
				new OwnershipGuard());
	}

	@Test
	void submitAnswer_acceptsEmptyAnswer_andRecordsItAsIncorrect() {
		User owner = userWithId(1L);
		Dictionary dictionary = new Dictionary(owner, "Travel", "de", "hr");
		Card card = new Card(dictionary, CardType.WORD, "Hund", 0);
		ReflectionTestUtils.setField(card, "id", 5L);
		card.setPrimaryTranslation("dog");
		card.setAcceptedAnswers(List.of("dog"));

		Game game = new Game(owner, dictionary, GameStatus.IN_PROGRESS, 1);
		ReflectionTestUtils.setField(game, "id", 7L);
		game.setAnsweredCards(0);
		game.setCorrectAnswers(0);
		game.setIncorrectAnswers(0);
		game.setDeckCardIds(List.of(5L));

		when(currentUserProvider.getUserId()).thenReturn(1L);
		when(gameRepository.findById(7L)).thenReturn(Optional.of(game));
		when(cardRepository.findById(5L)).thenReturn(Optional.of(card));
		when(answerValidator.validate(eq(List.of("dog")), eq(CardType.WORD), eq(""))).thenReturn(ValidationResult.WRONG_TRANSLATION);
		when(cardProgressRepository.findByUserIdAndCardId(1L, 5L)).thenReturn(Optional.empty());
		when(userRepository.getReferenceById(1L)).thenReturn(owner);
		when(gameMapper.toCardRevealResponse(card))
				.thenReturn(new CardRevealResponse("die", "Hund", "dog", null, null, null, null));

		AnswerValidationResponse response = service.submitAnswer(7L, new SubmitAnswerRequest(5L, "", 1000L));

		assertThat(response.correct()).isFalse();
		assertThat(response.givenAnswer()).isEqualTo("");
		assertThat(response.revealedCard()).isNotNull();
		assertThat(game.getIncorrectAnswers()).isEqualTo(1);
		assertThat(game.getAnsweredCards()).isEqualTo(1);
	}

	private User userWithId(Long id) {
		User user = new User("user" + id + "@example.com", "hash", "User " + id);
		ReflectionTestUtils.setField(user, "id", id);
		return user;
	}
}
