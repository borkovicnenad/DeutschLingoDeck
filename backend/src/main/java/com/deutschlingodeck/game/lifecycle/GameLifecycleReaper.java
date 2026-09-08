package com.deutschlingodeck.game.lifecycle;

import com.deutschlingodeck.game.dto.GameStatus;
import com.deutschlingodeck.game.entity.Game;
import com.deutschlingodeck.game.repository.GameRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Safety net for games the client never explicitly finished or abandoned: a browser crash, a
 * force-quit, or a lost network connection all skip the client-side abandon-on-{@code pagehide}
 * call. Runs periodically and marks any {@code IN_PROGRESS} game whose {@code lastActivityAt} is
 * older than {@code app.game.idle-timeout} as {@code ABANDONED}, so it stops counting as an active
 * session and frees the user to start a new one.
 */
@Component
public class GameLifecycleReaper {

	private static final long CHECK_INTERVAL_MS = 5 * 60 * 1000;

	private final GameRepository gameRepository;
	private final Duration idleTimeout;

	public GameLifecycleReaper(
			GameRepository gameRepository,
			@Value("${app.game.idle-timeout:PT30M}") Duration idleTimeout) {
		this.gameRepository = gameRepository;
		this.idleTimeout = idleTimeout;
	}

	@Scheduled(fixedDelay = CHECK_INTERVAL_MS)
	@Transactional
	public void abandonIdleGames() {
		OffsetDateTime cutoff = OffsetDateTime.now().minus(idleTimeout);
		List<Game> idleGames = gameRepository.findByStatusAndLastActivityAtBefore(GameStatus.IN_PROGRESS, cutoff);
		for (Game game : idleGames) {
			game.setStatus(GameStatus.ABANDONED);
			game.setFinishedAt(game.getLastActivityAt());
		}
		gameRepository.saveAll(idleGames);
	}
}
