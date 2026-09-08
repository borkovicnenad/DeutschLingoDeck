package com.deutschlingodeck.statistics.service;

import com.deutschlingodeck.common.exception.ResourceNotFoundException;
import com.deutschlingodeck.common.pagination.PageMapper;
import com.deutschlingodeck.common.pagination.PageResponse;
import com.deutschlingodeck.common.security.CurrentUserProvider;
import com.deutschlingodeck.common.security.OwnershipGuard;
import com.deutschlingodeck.dictionary.entity.Card;
import com.deutschlingodeck.dictionary.entity.Dictionary;
import com.deutschlingodeck.dictionary.repository.CardRepository;
import com.deutschlingodeck.dictionary.repository.DictionaryRepository;
import com.deutschlingodeck.game.dto.GameStatus;
import com.deutschlingodeck.game.entity.Game;
import com.deutschlingodeck.game.progress.CardProgress;
import com.deutschlingodeck.game.progress.CardProgressRepository;
import com.deutschlingodeck.game.repository.CardAnswerAggregate;
import com.deutschlingodeck.game.repository.GameAnswerRepository;
import com.deutschlingodeck.game.repository.GameRepository;
import com.deutschlingodeck.gamification.entity.UserGamificationStats;
import com.deutschlingodeck.gamification.repository.UserGamificationStatsRepository;
import com.deutschlingodeck.statistics.dto.CardStatisticsResponse;
import com.deutschlingodeck.statistics.dto.DashboardStatisticsResponse;
import com.deutschlingodeck.statistics.dto.DictionaryStatisticsResponse;
import com.deutschlingodeck.statistics.dto.LearningHistoryResponse;
import com.deutschlingodeck.statistics.dto.MasteryBreakdownResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

	private static final int DIFFICULT_CARD_MIN_ATTEMPTS = 3;
	private static final double DIFFICULT_CARD_SUCCESS_RATE_THRESHOLD = 0.5;
	private static final LocalDate EARLIEST_HISTORY_DATE = LocalDate.of(2000, 1, 1);

	private final DictionaryRepository dictionaryRepository;
	private final CardRepository cardRepository;
	private final GameRepository gameRepository;
	private final GameAnswerRepository gameAnswerRepository;
	private final CardProgressRepository cardProgressRepository;
	private final UserGamificationStatsRepository gamificationStatsRepository;
	private final CurrentUserProvider currentUserProvider;
	private final OwnershipGuard ownershipGuard;

	public StatisticsServiceImpl(
			DictionaryRepository dictionaryRepository,
			CardRepository cardRepository,
			GameRepository gameRepository,
			GameAnswerRepository gameAnswerRepository,
			CardProgressRepository cardProgressRepository,
			UserGamificationStatsRepository gamificationStatsRepository,
			CurrentUserProvider currentUserProvider,
			OwnershipGuard ownershipGuard) {
		this.dictionaryRepository = dictionaryRepository;
		this.cardRepository = cardRepository;
		this.gameRepository = gameRepository;
		this.gameAnswerRepository = gameAnswerRepository;
		this.cardProgressRepository = cardProgressRepository;
		this.gamificationStatsRepository = gamificationStatsRepository;
		this.currentUserProvider = currentUserProvider;
		this.ownershipGuard = ownershipGuard;
	}

	@Override
	public DashboardStatisticsResponse getDashboardStatistics(LocalDate from, LocalDate to, Long dictionaryId, String gameMode) {
		Long userId = currentUserProvider.getUserId();
		if (dictionaryId != null) {
			requireOwnedDictionary(dictionaryId, userId);
		}
		OffsetDateTime[] range = resolveRange(from, to);

		List<Game> games = gameRepository.findForStatistics(userId, range[0], range[1], dictionaryId, gameMode);

		// Derived from each game's own running counters rather than a separate GameAnswer query,
		// so the numbers always reflect exactly the same set of games the dictionary/game-mode
		// filters selected above.
		int totalCorrect = games.stream().mapToInt(game -> orZero(game.getCorrectAnswers())).sum();
		int totalIncorrect = games.stream().mapToInt(game -> orZero(game.getIncorrectAnswers())).sum();
		int totalAnswers = totalCorrect + totalIncorrect;
		Double overallAccuracy = totalAnswers == 0 ? null : (double) totalCorrect / totalAnswers;

		long totalStudyTimeSeconds = games.stream()
				.filter(game -> game.getFinishedAt() != null)
				.mapToLong(game -> Duration.between(game.getStartedAt(), game.getFinishedAt()).getSeconds())
				.sum();

		UserGamificationStats stats = gamificationStatsRepository.findById(userId).orElse(null);
		int currentStreak = stats == null ? 0 : stats.getCurrentStreak();
		int longestStreak = stats == null ? 0 : stats.getLongestStreak();

		long cardsDueToday = cardProgressRepository.countDueForUser(userId, LocalDate.now());

		return new DashboardStatisticsResponse(
				games.size(), totalAnswers, totalCorrect, totalIncorrect, overallAccuracy,
				totalStudyTimeSeconds, currentStreak, longestStreak, cardsDueToday);
	}

	@Override
	public PageResponse<CardStatisticsResponse> getCardStatistics(Long dictionaryId, LocalDate from, LocalDate to, int page, int size) {
		Long userId = currentUserProvider.getUserId();
		Pageable pageable = PageRequest.of(page, size);

		Page<Card> cards;
		if (dictionaryId != null) {
			requireOwnedDictionary(dictionaryId, userId);
			cards = cardRepository.findByDictionaryId(dictionaryId, pageable);
		} else {
			cards = cardRepository.findByDictionaryOwnerId(userId, pageable);
		}

		OffsetDateTime[] range = resolveRange(from, to);
		Map<Long, CardAnswerAggregate> aggregates =
				aggregateFor(userId, cards.getContent().stream().map(Card::getId).toList(), range[0], range[1]);
		return PageMapper.toPageResponse(cards, card -> toCardStatisticsResponse(card, aggregates.get(card.getId())));
	}

	@Override
	public List<DictionaryStatisticsResponse> getDictionaryStatistics() {
		Long userId = currentUserProvider.getUserId();
		Page<Dictionary> dictionaries = dictionaryRepository.findByOwnerIdAndDeletedFalse(userId, Pageable.unpaged());
		return dictionaries.getContent().stream().map(dictionary -> toDictionaryStatistics(userId, dictionary)).toList();
	}

	@Override
	public MasteryBreakdownResponse getMasteryBreakdown(Long dictionaryId) {
		Long userId = currentUserProvider.getUserId();
		if (dictionaryId != null) {
			requireOwnedDictionary(dictionaryId, userId);
		}

		int totalCards = dictionaryId != null
				? cardRepository.countByDictionaryId(dictionaryId)
				: cardRepository.countByDictionaryOwnerId(userId);

		int mastered = (int) cardProgressRepository.countByIntervalDaysRange(
				userId, dictionaryId, CardProgress.MASTERED_INTERVAL_DAYS_THRESHOLD, Integer.MAX_VALUE);
		int strong = (int) cardProgressRepository.countByIntervalDaysRange(
				userId, dictionaryId, 7, CardProgress.MASTERED_INTERVAL_DAYS_THRESHOLD - 1);
		int familiar = (int) cardProgressRepository.countByIntervalDaysRange(userId, dictionaryId, 2, 6);
		int learning = (int) cardProgressRepository.countByIntervalDaysRange(userId, dictionaryId, 0, 1);
		int reviewed = (int) cardProgressRepository.countAllForUser(userId, dictionaryId);
		int newCount = Math.max(0, totalCards - reviewed);

		return new MasteryBreakdownResponse(newCount, learning, familiar, strong, mastered, totalCards);
	}

	@Override
	public PageResponse<LearningHistoryResponse> getLearningHistory(
			LocalDate from, LocalDate to, Long dictionaryId, String gameMode, int page, int size) {
		Long userId = currentUserProvider.getUserId();
		if (dictionaryId != null) {
			requireOwnedDictionary(dictionaryId, userId);
		}
		OffsetDateTime[] range = resolveRange(from, to);
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startedAt"));
		Page<Game> games = gameRepository.findForStatistics(userId, range[0], range[1], dictionaryId, gameMode, pageable);
		return PageMapper.toPageResponse(games, this::toLearningHistoryResponse);
	}

	private void requireOwnedDictionary(Long dictionaryId, Long userId) {
		Dictionary dictionary = dictionaryRepository.findById(dictionaryId)
				.filter(d -> !d.isDeleted())
				.orElseThrow(() -> ResourceNotFoundException.of("Dictionary", dictionaryId));
		ownershipGuard.requireOwner(dictionary.getOwner().getId(), userId);
	}

	private DictionaryStatisticsResponse toDictionaryStatistics(Long userId, Dictionary dictionary) {
		Long dictionaryId = dictionary.getId();
		int completedGames = (int) gameRepository.countByUserIdAndDictionaryIdAndStatus(userId, dictionaryId, GameStatus.FINISHED);
		long totalAnswers = gameAnswerRepository.countByUserAndDictionary(userId, dictionaryId);
		long totalCorrect = gameAnswerRepository.countCorrectByUserAndDictionary(userId, dictionaryId);
		Double averageAccuracy = totalAnswers == 0 ? null : (double) totalCorrect / totalAnswers;

		int masteredCards = (int) cardProgressRepository.countMasteredForDictionary(
				userId, dictionaryId, CardProgress.MASTERED_INTERVAL_DAYS_THRESHOLD);

		List<Long> cardIds = cardRepository.findByDictionaryIdOrderByPosition(dictionaryId).stream().map(Card::getId).toList();
		OffsetDateTime[] allTime = resolveRange(null, null);
		long difficultCards = aggregateFor(userId, cardIds, allTime[0], allTime[1]).values().stream()
				.filter(agg -> agg.timesShown() >= DIFFICULT_CARD_MIN_ATTEMPTS)
				.filter(agg -> (double) agg.timesCorrect() / agg.timesShown() < DIFFICULT_CARD_SUCCESS_RATE_THRESHOLD)
				.count();

		return new DictionaryStatisticsResponse(
				dictionaryId, dictionary.getName(), completedGames, (int) totalAnswers, averageAccuracy,
				masteredCards, (int) difficultCards);
	}

	private Map<Long, CardAnswerAggregate> aggregateFor(Long userId, List<Long> cardIds, OffsetDateTime start, OffsetDateTime end) {
		if (cardIds.isEmpty()) {
			return Map.of();
		}
		return gameAnswerRepository.aggregateForCards(userId, cardIds, start, end).stream()
				.collect(Collectors.toMap(CardAnswerAggregate::cardId, Function.identity()));
	}

	private CardStatisticsResponse toCardStatisticsResponse(Card card, CardAnswerAggregate aggregate) {
		long timesShown = aggregate == null ? 0 : aggregate.timesShown();
		long timesCorrect = aggregate == null ? 0 : aggregate.timesCorrect();
		long timesIncorrect = timesShown - timesCorrect;
		Double successRate = timesShown == 0 ? null : (double) timesCorrect / timesShown;

		return new CardStatisticsResponse(
				card.getId(), card.getDictionary().getId(), card.getSourceText(),
				(int) timesShown, (int) timesCorrect, (int) timesIncorrect, successRate,
				aggregate == null ? null : aggregate.lastShownAt(),
				aggregate == null ? null : aggregate.lastCorrectAt(),
				aggregate == null ? null : aggregate.averageResponseTimeMs());
	}

	private LearningHistoryResponse toLearningHistoryResponse(Game game) {
		int correct = game.getCorrectAnswers() == null ? 0 : game.getCorrectAnswers();
		int incorrect = game.getIncorrectAnswers() == null ? 0 : game.getIncorrectAnswers();
		int answered = correct + incorrect;
		Double accuracy = answered == 0 ? null : (double) correct / answered;

		return new LearningHistoryResponse(
				game.getId(), game.getDictionary().getId(), game.getDictionary().getName(),
				game.getStatus(), game.getStartedAt(), game.getFinishedAt(), accuracy);
	}

	private int orZero(Integer value) {
		return value == null ? 0 : value;
	}

	private OffsetDateTime[] resolveRange(LocalDate from, LocalDate to) {
		ZoneOffset offset = OffsetDateTime.now().getOffset();
		LocalDate start = from != null ? from : EARLIEST_HISTORY_DATE;
		LocalDate end = to != null ? to : LocalDate.now();
		return new OffsetDateTime[] {OffsetDateTime.of(start, LocalTime.MIN, offset), OffsetDateTime.of(end, LocalTime.MAX, offset)};
	}
}
