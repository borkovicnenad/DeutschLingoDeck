package com.deutschlingodeck.game.service;

import com.deutschlingodeck.authentication.entity.User;
import com.deutschlingodeck.authentication.repository.UserRepository;
import com.deutschlingodeck.common.exception.ForbiddenException;
import com.deutschlingodeck.common.security.CurrentUserProvider;
import com.deutschlingodeck.common.security.OwnershipGuard;
import com.deutschlingodeck.dictionary.entity.Dictionary;
import com.deutschlingodeck.dictionary.repository.CardRepository;
import com.deutschlingodeck.dictionary.repository.DictionaryRepository;
import com.deutschlingodeck.game.dto.GameStatus;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/** Verifies that game ownership is enforced with the real {@link OwnershipGuard}. */
@ExtendWith(MockitoExtension.class)
class GameServiceOwnershipTest {

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
	void getGame_throwsForbidden_whenCurrentUserDidNotStartTheGame() {
		User gameOwner = userWithId(2L);
		Dictionary dictionary = new Dictionary(gameOwner, "Travel", "hr", "de");
		Game game = new Game(gameOwner, dictionary, GameStatus.IN_PROGRESS, 10);
		ReflectionTestUtils.setField(game, "id", 7L);

		when(currentUserProvider.getUserId()).thenReturn(1L);
		when(gameRepository.findById(7L)).thenReturn(Optional.of(game));

		assertThatThrownBy(() -> service.getGame(7L)).isInstanceOf(ForbiddenException.class);
	}

	private User userWithId(Long id) {
		User user = new User("user" + id + "@example.com", "hash", "User " + id);
		ReflectionTestUtils.setField(user, "id", id);
		return user;
	}
}
