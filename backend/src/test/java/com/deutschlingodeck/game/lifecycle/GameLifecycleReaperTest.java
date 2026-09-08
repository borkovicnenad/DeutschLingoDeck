package com.deutschlingodeck.game.lifecycle;

import com.deutschlingodeck.authentication.entity.User;
import com.deutschlingodeck.dictionary.entity.Dictionary;
import com.deutschlingodeck.game.dto.GameStatus;
import com.deutschlingodeck.game.entity.Game;
import com.deutschlingodeck.game.repository.GameRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameLifecycleReaperTest {

	@Mock
	private GameRepository gameRepository;

	@Test
	void abandonIdleGames_marksStaleInProgressGamesAsAbandoned() {
		User owner = new User("owner@example.com", "hash", "Owner");
		Dictionary dictionary = new Dictionary(owner, "Travel", "de", "hr");
		OffsetDateTime lastActivity = OffsetDateTime.now().minusHours(2);

		Game staleGame = new Game(owner, dictionary, GameStatus.IN_PROGRESS, 5);
		staleGame.setLastActivityAt(lastActivity);

		when(gameRepository.findByStatusAndLastActivityAtBefore(eq(GameStatus.IN_PROGRESS), any()))
				.thenReturn(List.of(staleGame));

		GameLifecycleReaper reaper = new GameLifecycleReaper(gameRepository, Duration.ofMinutes(30));
		reaper.abandonIdleGames();

		assertThat(staleGame.getStatus()).isEqualTo(GameStatus.ABANDONED);
		assertThat(staleGame.getFinishedAt()).isEqualTo(lastActivity);

		ArgumentCaptor<List<Game>> savedCaptor = ArgumentCaptor.forClass(List.class);
		verify(gameRepository).saveAll(savedCaptor.capture());
		assertThat(savedCaptor.getValue()).containsExactly(staleGame);
	}

	@Test
	void abandonIdleGames_leavesRecentlyActiveGamesAlone() {
		when(gameRepository.findByStatusAndLastActivityAtBefore(eq(GameStatus.IN_PROGRESS), any()))
				.thenReturn(List.of());

		GameLifecycleReaper reaper = new GameLifecycleReaper(gameRepository, Duration.ofMinutes(30));
		reaper.abandonIdleGames();

		verify(gameRepository).saveAll(List.of());
	}
}
