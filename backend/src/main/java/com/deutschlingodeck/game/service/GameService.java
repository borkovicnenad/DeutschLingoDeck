package com.deutschlingodeck.game.service;

import com.deutschlingodeck.common.pagination.PageResponse;
import com.deutschlingodeck.game.dto.AnswerValidationResponse;
import com.deutschlingodeck.game.dto.CreateGameRequest;
import com.deutschlingodeck.game.dto.GameResponse;
import com.deutschlingodeck.game.dto.GameStatus;
import com.deutschlingodeck.game.dto.GameSummaryResponse;
import com.deutschlingodeck.game.dto.SubmitAnswerRequest;

public interface GameService {

	GameResponse createGame(CreateGameRequest request);

	PageResponse<GameSummaryResponse> listGames(GameStatus status, Long dictionaryId, int page, int size);

	GameResponse getGame(Long gameId);

	AnswerValidationResponse submitAnswer(Long gameId, SubmitAnswerRequest request);

	GameSummaryResponse finishGame(Long gameId);

	GameSummaryResponse abandonGame(Long gameId);

	GameSummaryResponse getGameSummary(Long gameId);
}
