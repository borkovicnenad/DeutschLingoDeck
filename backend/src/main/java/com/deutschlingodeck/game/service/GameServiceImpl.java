package com.deutschlingodeck.game.service;

import com.deutschlingodeck.common.pagination.PageResponse;
import com.deutschlingodeck.game.dto.AnswerValidationResponse;
import com.deutschlingodeck.game.dto.CreateGameRequest;
import com.deutschlingodeck.game.dto.GameResponse;
import com.deutschlingodeck.game.dto.GameStatus;
import com.deutschlingodeck.game.dto.GameSummaryResponse;
import com.deutschlingodeck.game.dto.SubmitAnswerRequest;
import com.deutschlingodeck.game.repository.GameAnswerRepository;
import com.deutschlingodeck.game.repository.GameRepository;
import org.springframework.stereotype.Service;

@Service
public class GameServiceImpl implements GameService {

	private final GameRepository gameRepository;
	private final GameAnswerRepository gameAnswerRepository;

	public GameServiceImpl(GameRepository gameRepository, GameAnswerRepository gameAnswerRepository) {
		this.gameRepository = gameRepository;
		this.gameAnswerRepository = gameAnswerRepository;
	}

	@Override
	public GameResponse createGame(CreateGameRequest request) {
		// TODO: load the dictionary, build the card deck for the requested gameMode
		// and persist a new Game in CREATED/STARTED status.
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public PageResponse<GameSummaryResponse> listGames(GameStatus status, Long dictionaryId, int page, int size) {
		// TODO: page games for the current user, optionally filtered by status/dictionaryId.
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public GameResponse getGame(Long gameId) {
		// TODO: load the game, verify ownership, map to GameResponse including the current card.
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public AnswerValidationResponse submitAnswer(Long gameId, SubmitAnswerRequest request) {
		// TODO: validate the given answer against the card's accepted answers,
		// persist a GameAnswer via gameAnswerRepository and advance to the next card.
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public GameSummaryResponse finishGame(Long gameId) {
		// TODO: mark the game as FINISHED and compute the summary statistics.
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public GameSummaryResponse abandonGame(Long gameId) {
		// TODO: mark the game as ABANDONED and compute the summary statistics.
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public GameSummaryResponse getGameSummary(Long gameId) {
		// TODO: load the game and compute/return its summary statistics.
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
