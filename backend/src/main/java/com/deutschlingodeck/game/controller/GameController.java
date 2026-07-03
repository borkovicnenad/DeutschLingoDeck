package com.deutschlingodeck.game.controller;

import com.deutschlingodeck.common.constants.ApiConstants;
import com.deutschlingodeck.common.pagination.PageResponse;
import com.deutschlingodeck.game.dto.AnswerValidationResponse;
import com.deutschlingodeck.game.dto.CreateGameRequest;
import com.deutschlingodeck.game.dto.GameResponse;
import com.deutschlingodeck.game.dto.GameStatus;
import com.deutschlingodeck.game.dto.GameSummaryResponse;
import com.deutschlingodeck.game.dto.SubmitAnswerRequest;
import com.deutschlingodeck.game.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.API_BASE_PATH + "/games")
public class GameController {

	private final GameService gameService;

	public GameController(GameService gameService) {
		this.gameService = gameService;
	}

	@PostMapping
	public ResponseEntity<GameResponse> createGame(@Valid @RequestBody CreateGameRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(gameService.createGame(request));
	}

	@GetMapping
	public ResponseEntity<PageResponse<GameSummaryResponse>> listGames(
			@RequestParam(required = false) GameStatus status,
			@RequestParam(required = false) Long dictionaryId,
			@RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE) int page,
			@RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE_SIZE) int size) {
		return ResponseEntity.ok(gameService.listGames(status, dictionaryId, page, size));
	}

	@GetMapping("/{gameId}")
	public ResponseEntity<GameResponse> getGame(@PathVariable Long gameId) {
		return ResponseEntity.ok(gameService.getGame(gameId));
	}

	@PostMapping("/{gameId}/answers")
	public ResponseEntity<AnswerValidationResponse> submitAnswer(
			@PathVariable Long gameId, @Valid @RequestBody SubmitAnswerRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(gameService.submitAnswer(gameId, request));
	}

	@PostMapping("/{gameId}/finish")
	public ResponseEntity<GameSummaryResponse> finishGame(@PathVariable Long gameId) {
		return ResponseEntity.ok(gameService.finishGame(gameId));
	}

	@PostMapping("/{gameId}/abandon")
	public ResponseEntity<GameSummaryResponse> abandonGame(@PathVariable Long gameId) {
		return ResponseEntity.ok(gameService.abandonGame(gameId));
	}

	@GetMapping("/{gameId}/summary")
	public ResponseEntity<GameSummaryResponse> getGameSummary(@PathVariable Long gameId) {
		return ResponseEntity.ok(gameService.getGameSummary(gameId));
	}
}
