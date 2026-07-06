package com.deutschlingodeck.gamification.service;

import com.deutschlingodeck.authentication.repository.UserRepository;
import com.deutschlingodeck.common.exception.UnauthorizedException;
import com.deutschlingodeck.game.dto.GameStatus;
import com.deutschlingodeck.game.progress.CardProgress;
import com.deutschlingodeck.game.progress.CardProgressRepository;
import com.deutschlingodeck.game.repository.GameAnswerRepository;
import com.deutschlingodeck.game.repository.GameRepository;
import com.deutschlingodeck.gamification.dto.AchievementDefinition;
import com.deutschlingodeck.gamification.dto.AchievementResponse;
import com.deutschlingodeck.gamification.dto.DailyGoalResponse;
import com.deutschlingodeck.gamification.dto.LevelProgressResponse;
import com.deutschlingodeck.gamification.dto.WeeklyActivityPointResponse;
import com.deutschlingodeck.gamification.entity.UserAchievement;
import com.deutschlingodeck.gamification.entity.UserGamificationStats;
import com.deutschlingodeck.gamification.repository.UserAchievementRepository;
import com.deutschlingodeck.gamification.repository.UserGamificationStatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GamificationServiceImpl implements GamificationService {

	private static final int XP_PER_CORRECT_ANSWER = 10;
	private static final int XP_PER_INCORRECT_ANSWER = 2;
	private static final int XP_PER_LEVEL = 150;
	private static final int MASTERED_ACHIEVEMENT_THRESHOLD = 100;
	private static final int POLYGLOT_PACE_THRESHOLD = 50;
	private static final int NIGHT_OWL_HOUR_CUTOFF = 5;
	private static final List<String> LEVEL_TITLES = List.of(
			"Anfänger", "Lernender", "Fortgeschritten", "Wortschatz-Kenner", "Sprachprofi", "Meister", "Legende");

	private final UserGamificationStatsRepository statsRepository;
	private final UserAchievementRepository achievementRepository;
	private final UserRepository userRepository;
	private final GameRepository gameRepository;
	private final GameAnswerRepository gameAnswerRepository;
	private final CardProgressRepository cardProgressRepository;

	public GamificationServiceImpl(
			UserGamificationStatsRepository statsRepository,
			UserAchievementRepository achievementRepository,
			UserRepository userRepository,
			GameRepository gameRepository,
			GameAnswerRepository gameAnswerRepository,
			CardProgressRepository cardProgressRepository) {
		this.statsRepository = statsRepository;
		this.achievementRepository = achievementRepository;
		this.userRepository = userRepository;
		this.gameRepository = gameRepository;
		this.gameAnswerRepository = gameAnswerRepository;
		this.cardProgressRepository = cardProgressRepository;
	}

	@Override
	@Transactional
	public void recordAnswer(Long userId, boolean correct, OffsetDateTime answeredAt) {
		UserGamificationStats stats = getOrCreateStats(userId);
		stats.setTotalXp(stats.getTotalXp() + (correct ? XP_PER_CORRECT_ANSWER : XP_PER_INCORRECT_ANSWER));

		LocalDate today = answeredAt.toLocalDate();
		if (!today.equals(stats.getLastActivityDate())) {
			if (today.minusDays(1).equals(stats.getLastActivityDate())) {
				stats.setCurrentStreak(stats.getCurrentStreak() + 1);
			} else {
				stats.setCurrentStreak(1);
			}
			stats.setLongestStreak(Math.max(stats.getLongestStreak(), stats.getCurrentStreak()));
			stats.setLastActivityDate(today);
		}
		statsRepository.save(stats);
	}

	@Override
	@Transactional
	public void evaluateAchievements(Long userId, boolean perfectGame, OffsetDateTime finishedAt) {
		UserGamificationStats stats = getOrCreateStats(userId);

		if (gameRepository.countByUserIdAndStatus(userId, GameStatus.FINISHED) == 1) {
			unlock(userId, AchievementDefinition.FIRST_STEPS);
		}
		if (stats.getCurrentStreak() >= 7) {
			unlock(userId, AchievementDefinition.WEEK_STREAK);
		}
		if (perfectGame) {
			unlock(userId, AchievementDefinition.PERFECT_GAME);
		}
		if (finishedAt.getHour() < NIGHT_OWL_HOUR_CUTOFF) {
			unlock(userId, AchievementDefinition.NIGHT_OWL);
		}

		OffsetDateTime[] today = dayRange(finishedAt.toLocalDate(), finishedAt.getOffset());
		if (gameAnswerRepository.countByUserAndAnsweredAtBetween(userId, today[0], today[1]) >= POLYGLOT_PACE_THRESHOLD) {
			unlock(userId, AchievementDefinition.POLYGLOT_PACE);
		}
		if (cardProgressRepository.countMasteredForUser(userId, CardProgress.MASTERED_INTERVAL_DAYS_THRESHOLD) >= MASTERED_ACHIEVEMENT_THRESHOLD) {
			unlock(userId, AchievementDefinition.HUNDRED_MASTERED);
		}
	}

	@Override
	public LevelProgressResponse getLevelProgress(Long userId) {
		long totalXp = getOrCreateStats(userId).getTotalXp();
		int level = (int) (totalXp / XP_PER_LEVEL) + 1;
		long xpToNextLevel = (long) level * XP_PER_LEVEL;
		String title = LEVEL_TITLES.get(Math.min(level - 1, LEVEL_TITLES.size() - 1));
		return new LevelProgressResponse(level, title, totalXp, xpToNextLevel);
	}

	@Override
	public DailyGoalResponse getDailyGoal(Long userId) {
		UserGamificationStats stats = getOrCreateStats(userId);
		OffsetDateTime[] today = dayRange(LocalDate.now(), OffsetDateTime.now().getOffset());
		long completed = gameAnswerRepository.countByUserAndAnsweredAtBetween(userId, today[0], today[1]);
		return new DailyGoalResponse(stats.getDailyGoalTarget(), (int) completed);
	}

	@Override
	public List<AchievementResponse> getAchievements(Long userId) {
		Map<String, UserAchievement> unlocked = achievementRepository.findByUserId(userId).stream()
				.collect(Collectors.toMap(UserAchievement::getAchievementCode, Function.identity()));

		return Arrays.stream(AchievementDefinition.values())
				.map(definition -> {
					UserAchievement entry = unlocked.get(definition.name());
					return new AchievementResponse(
							definition.name(), definition.getTitle(), definition.getDescription(), definition.getIcon(),
							entry != null, entry != null ? entry.getUnlockedAt() : null);
				})
				.toList();
	}

	@Override
	public List<WeeklyActivityPointResponse> getWeeklyActivity(Long userId) {
		ZoneOffset offset = OffsetDateTime.now().getOffset();
		List<WeeklyActivityPointResponse> points = new ArrayList<>();
		for (int daysAgo = 6; daysAgo >= 0; daysAgo--) {
			LocalDate day = LocalDate.now().minusDays(daysAgo);
			OffsetDateTime[] range = dayRange(day, offset);
			long count = gameAnswerRepository.countByUserAndAnsweredAtBetween(userId, range[0], range[1]);
			points.add(new WeeklyActivityPointResponse(day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH), (int) count));
		}
		return points;
	}

	private void unlock(Long userId, AchievementDefinition achievement) {
		if (!achievementRepository.existsByUserIdAndAchievementCode(userId, achievement.name())) {
			achievementRepository.save(
					new UserAchievement(userRepository.getReferenceById(userId), achievement.name(), OffsetDateTime.now()));
		}
	}

	private UserGamificationStats getOrCreateStats(Long userId) {
		// UserGamificationStats.userId uses @MapsId against the `user` association, which
		// Hibernate derives from the associated entity's actual (loaded) identifier - an
		// uninitialized getReferenceById() proxy isn't enough here and throws.
		return statsRepository.findById(userId)
				.orElseGet(() -> statsRepository.save(new UserGamificationStats(
						userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("Authentication is required")))));
	}

	private OffsetDateTime[] dayRange(LocalDate day, ZoneOffset offset) {
		OffsetDateTime start = OffsetDateTime.of(day, LocalTime.MIN, offset);
		return new OffsetDateTime[] {start, start.plusDays(1)};
	}
}
