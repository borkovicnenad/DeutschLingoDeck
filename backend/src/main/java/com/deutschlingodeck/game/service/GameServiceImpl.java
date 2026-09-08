package com.deutschlingodeck.game.service;

import com.deutschlingodeck.authentication.entity.User;
import com.deutschlingodeck.authentication.repository.UserRepository;
import com.deutschlingodeck.common.exception.ConflictException;
import com.deutschlingodeck.common.exception.ResourceNotFoundException;
import com.deutschlingodeck.common.pagination.PageMapper;
import com.deutschlingodeck.common.pagination.PageResponse;
import com.deutschlingodeck.common.security.CurrentUserProvider;
import com.deutschlingodeck.common.security.OwnershipGuard;
import com.deutschlingodeck.dictionary.entity.Card;
import com.deutschlingodeck.dictionary.entity.Dictionary;
import com.deutschlingodeck.dictionary.repository.CardRepository;
import com.deutschlingodeck.dictionary.repository.DictionaryRepository;
import com.deutschlingodeck.game.dto.AnswerValidationResponse;
import com.deutschlingodeck.game.dto.CardRevealResponse;
import com.deutschlingodeck.game.dto.CreateGameRequest;
import com.deutschlingodeck.game.dto.CurrentCardResponse;
import com.deutschlingodeck.game.dto.GameResponse;
import com.deutschlingodeck.game.dto.GameStatus;
import com.deutschlingodeck.game.dto.GameSummaryResponse;
import com.deutschlingodeck.game.dto.SubmitAnswerRequest;
import com.deutschlingodeck.game.dto.ValidationResult;
import com.deutschlingodeck.game.engine.DeckBuilder;
import com.deutschlingodeck.game.engine.RequeuePolicy;
import com.deutschlingodeck.game.entity.Game;
import com.deutschlingodeck.game.entity.GameAnswer;
import com.deutschlingodeck.game.mapper.GameMapper;
import com.deutschlingodeck.game.progress.CardProgress;
import com.deutschlingodeck.game.progress.CardProgressRepository;
import com.deutschlingodeck.game.progress.SpacedRepetitionScheduler;
import com.deutschlingodeck.game.repository.GameAnswerRepository;
import com.deutschlingodeck.game.repository.GameRepository;
import com.deutschlingodeck.gamification.service.GamificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class GameServiceImpl implements GameService {

	private static final String DEFAULT_GAME_MODE = "STANDARD";

	/** The language a card's {@code article}/{@code sourceText} are always recorded in. */
	private static final String GERMAN_LANGUAGE = "de";

	private final GameRepository gameRepository;
	private final GameAnswerRepository gameAnswerRepository;
	private final DictionaryRepository dictionaryRepository;
	private final CardRepository cardRepository;
	private final CardProgressRepository cardProgressRepository;
	private final UserRepository userRepository;
	private final GameMapper gameMapper;
	private final AnswerValidator answerValidator;
	private final SpacedRepetitionScheduler scheduler;
	private final DeckBuilder deckBuilder;
	private final RequeuePolicy requeuePolicy;
	private final GamificationService gamificationService;
	private final CurrentUserProvider currentUserProvider;
	private final OwnershipGuard ownershipGuard;

	public GameServiceImpl(
			GameRepository gameRepository,
			GameAnswerRepository gameAnswerRepository,
			DictionaryRepository dictionaryRepository,
			CardRepository cardRepository,
			CardProgressRepository cardProgressRepository,
			UserRepository userRepository,
			GameMapper gameMapper,
			AnswerValidator answerValidator,
			SpacedRepetitionScheduler scheduler,
			DeckBuilder deckBuilder,
			RequeuePolicy requeuePolicy,
			GamificationService gamificationService,
			CurrentUserProvider currentUserProvider,
			OwnershipGuard ownershipGuard) {
		this.gameRepository = gameRepository;
		this.gameAnswerRepository = gameAnswerRepository;
		this.dictionaryRepository = dictionaryRepository;
		this.cardRepository = cardRepository;
		this.cardProgressRepository = cardProgressRepository;
		this.userRepository = userRepository;
		this.gameMapper = gameMapper;
		this.answerValidator = answerValidator;
		this.scheduler = scheduler;
		this.deckBuilder = deckBuilder;
		this.requeuePolicy = requeuePolicy;
		this.gamificationService = gamificationService;
		this.currentUserProvider = currentUserProvider;
		this.ownershipGuard = ownershipGuard;
	}

	@Override
	@Transactional
	public GameResponse createGame(CreateGameRequest request) {
		Long userId = currentUserProvider.getUserId();
		Dictionary dictionary = dictionaryRepository.findById(request.dictionaryId())
				.filter(d -> !d.isDeleted())
				.orElseThrow(() -> ResourceNotFoundException.of("Dictionary", request.dictionaryId()));
		ownershipGuard.requireOwner(dictionary.getOwner().getId(), userId);

		// Starting a new game completes any stale one instead of leaving it stuck IN_PROGRESS
		// forever; the partial unique index on (user_id) WHERE status = 'IN_PROGRESS' is the
		// last-resort guard against a race between two concurrent createGame calls. The abandon
		// must be flushed before the insert below: Game's IDENTITY id generation forces Hibernate
		// to issue that INSERT immediately rather than at end-of-transaction, so without an
		// explicit flush here it can race ahead of this pending UPDATE and trip the unique index.
		OffsetDateTime now = OffsetDateTime.now();
		gameRepository.findByUserIdAndStatus(userId, GameStatus.IN_PROGRESS).ifPresent(stale -> {
			stale.setStatus(GameStatus.ABANDONED);
			stale.setFinishedAt(now);
			gameRepository.saveAndFlush(stale);
		});

		List<Card> deck = deckBuilder.buildDeck(userId, dictionary.getId());
		if (deck.isEmpty()) {
			throw new ConflictException("This dictionary has no cards yet - add or import some before starting a game");
		}

		User user = userRepository.getReferenceById(userId);
		Game game = new Game(user, dictionary, GameStatus.IN_PROGRESS, deck.size());
		game.setAnsweredCards(0);
		game.setCorrectAnswers(0);
		game.setIncorrectAnswers(0);
		game.setStartedAt(now);
		game.setLastActivityAt(now);
		game.setGameMode(request.gameMode() == null || request.gameMode().isBlank() ? DEFAULT_GAME_MODE : request.gameMode());
		game.setDeckCardIds(deck.stream().map(Card::getId).toList());
		game = gameRepository.save(game);

		return toGameResponse(game);
	}

	@Override
	public PageResponse<GameSummaryResponse> listGames(GameStatus status, Long dictionaryId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<Game> games = gameRepository.search(currentUserProvider.getUserId(), status, dictionaryId, pageable);
		return PageMapper.toPageResponse(games, this::toGameSummaryResponse);
	}

	@Override
	public GameResponse getGame(Long gameId) {
		return toGameResponse(loadOwnedGame(gameId));
	}

	@Override
	@Transactional
	public AnswerValidationResponse submitAnswer(Long gameId, SubmitAnswerRequest request) {
		Long userId = currentUserProvider.getUserId();
		Game game = loadOwnedGame(gameId);
		requireInProgress(game);

		List<Long> deckCardIds = game.getDeckCardIds();
		int index = game.getAnsweredCards();
		if (index >= deckCardIds.size()) {
			throw new ConflictException("All cards in this game have already been answered");
		}
		Long expectedCardId = deckCardIds.get(index);
		if (!expectedCardId.equals(request.cardId())) {
			throw new ConflictException("The submitted card is not the current card for this game");
		}

		Card card = cardRepository.findById(request.cardId())
				.orElseThrow(() -> ResourceNotFoundException.of("Card", request.cardId()));

		List<String> expected = expectedAnswers(card);
		ValidationResult result = answerValidator.validate(expected, card.getCardType(), request.answer());
		boolean correct = result == ValidationResult.CORRECT;
		OffsetDateTime now = OffsetDateTime.now();

		GameAnswer answer = new GameAnswer(game, card, request.answer(), result, correct, request.responseTimeMs());
		answer.setAnsweredAt(now);
		gameAnswerRepository.save(answer);

		requeuePolicy.onAnswer(game, card.getId(), correct, index);
		game.setAnsweredCards(index + 1);
		game.setCorrectAnswers(game.getCorrectAnswers() + (correct ? 1 : 0));
		game.setIncorrectAnswers(game.getIncorrectAnswers() + (correct ? 0 : 1));
		game.setLastActivityAt(now);

		CardProgress progress = cardProgressRepository.findByUserIdAndCardId(userId, card.getId())
				.orElseGet(() -> new CardProgress(userRepository.getReferenceById(userId), card));
		scheduler.schedule(progress, result, now);
		cardProgressRepository.save(progress);

		gamificationService.recordAnswer(userId, correct, now);

		String expectedAnswer = expected.isEmpty() ? null : expected.get(0);

		return new AnswerValidationResponse(
				result, correct, request.answer(), expectedAnswer, feedbackMessage(result),
				gameMapper.toCardRevealResponse(card), resolveCurrentCard(game));
	}

	@Override
	@Transactional
	public GameSummaryResponse finishGame(Long gameId) {
		Long userId = currentUserProvider.getUserId();
		Game game = loadOwnedGame(gameId);
		requireInProgress(game);

		game.setStatus(GameStatus.FINISHED);
		game.setFinishedAt(OffsetDateTime.now());

		boolean perfectGame = game.getAnsweredCards() > 0 && game.getIncorrectAnswers() == 0;
		gamificationService.evaluateAchievements(userId, perfectGame, game.getFinishedAt());

		return buildSummary(game);
	}

	@Override
	@Transactional
	public GameSummaryResponse abandonGame(Long gameId) {
		Game game = loadOwnedGame(gameId);
		requireInProgress(game);

		game.setStatus(GameStatus.ABANDONED);
		game.setFinishedAt(OffsetDateTime.now());

		return buildSummary(game);
	}

	@Override
	public GameSummaryResponse getGameSummary(Long gameId) {
		return buildSummary(loadOwnedGame(gameId));
	}

	private Game loadOwnedGame(Long gameId) {
		Game game = gameRepository.findById(gameId).orElseThrow(() -> ResourceNotFoundException.of("Game", gameId));
		ownershipGuard.requireOwner(game.getUser().getId(), currentUserProvider.getUserId());
		return game;
	}

	private void requireInProgress(Game game) {
		if (game.getStatus() != GameStatus.IN_PROGRESS) {
			throw new ConflictException("This game is not currently in progress");
		}
	}

	private CurrentCardResponse resolveCurrentCard(Game game) {
		List<Long> deckCardIds = game.getDeckCardIds();
		int index = game.getAnsweredCards();
		if (index >= deckCardIds.size()) {
			return null;
		}
		Long cardId = deckCardIds.get(index);
		Card card = cardRepository.findById(cardId).orElseThrow(() -> ResourceNotFoundException.of("Card", cardId));

		if (isGermanRecall(card.getDictionary())) {
			return new CurrentCardResponse(
					card.getId(), card.getCardType(), null, null, card.getPrimaryTranslation(), null, null);
		}
		return new CurrentCardResponse(
				card.getId(), card.getCardType(), card.getArticle(), card.getSourceText(), null,
				card.getExample(), card.getGrammarInfo());
	}

	/** {@code true} when this dictionary's direction is "recall German" (translation shown first). */
	private boolean isGermanRecall(Dictionary dictionary) {
		return !GERMAN_LANGUAGE.equalsIgnoreCase(dictionary.getSourceLanguage());
	}

	/** The answer(s) that count as correct for this card, given its dictionary's learning direction. */
	private List<String> expectedAnswers(Card card) {
		if (isGermanRecall(card.getDictionary())) {
			String germanForm = card.getArticle() != null
					? card.getArticle() + " " + card.getSourceText()
					: card.getSourceText();
			return List.of(germanForm);
		}
		if (card.getAcceptedAnswers() != null && !card.getAcceptedAnswers().isEmpty()) {
			return card.getAcceptedAnswers();
		}
		return card.getPrimaryTranslation() != null ? List.of(card.getPrimaryTranslation()) : List.of();
	}

	private GameResponse toGameResponse(Game game) {
		GameResponse base = gameMapper.toGameResponse(game);
		return new GameResponse(
				base.id(), base.dictionaryId(), base.status(), base.totalCards(), base.answeredCards(),
				base.correctAnswers(), base.incorrectAnswers(), resolveCurrentCard(game));
	}

	private GameSummaryResponse toGameSummaryResponse(Game game) {
		return buildSummary(game);
	}

	private GameSummaryResponse buildSummary(Game game) {
		GameSummaryResponse base = gameMapper.toGameSummaryResponse(game);
		int correct = game.getCorrectAnswers() == null ? 0 : game.getCorrectAnswers();
		int incorrect = game.getIncorrectAnswers() == null ? 0 : game.getIncorrectAnswers();
		int answered = correct + incorrect;
		Double accuracy = answered == 0 ? null : (double) correct / answered;
		Long durationSeconds = game.getFinishedAt() == null
				? null
				: Duration.between(game.getStartedAt(), game.getFinishedAt()).getSeconds();
		Double averageResponseTimeMs = gameAnswerRepository.averageResponseTimeMsByGame(game.getId());

		return new GameSummaryResponse(
				base.gameId(), base.status(), base.totalCards(), base.answeredCards(), correct, incorrect,
				accuracy, durationSeconds, averageResponseTimeMs);
	}

	private String feedbackMessage(ValidationResult result) {
		return switch (result) {
			case CORRECT -> "Correct!";
			case WRONG_ARTICLE -> "Almost - check the article.";
			case TYPO -> "Close - looks like a typo.";
			case PARTIALLY_CORRECT -> "Partially correct.";
			case MISSING_WORD -> "Your answer is missing a word.";
			case EXTRA_WORD -> "Your answer has an extra word.";
			case WRONG_SENTENCE -> "Not quite the right sentence.";
			case WRONG_TRANSLATION -> "That's not the right translation.";
		};
	}
}
