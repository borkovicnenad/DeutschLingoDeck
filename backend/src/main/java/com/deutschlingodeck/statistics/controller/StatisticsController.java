package com.deutschlingodeck.statistics.controller;

import com.deutschlingodeck.common.constants.ApiConstants;
import com.deutschlingodeck.common.pagination.PageResponse;
import com.deutschlingodeck.statistics.dto.CardStatisticsResponse;
import com.deutschlingodeck.statistics.dto.DashboardStatisticsResponse;
import com.deutschlingodeck.statistics.dto.DictionaryStatisticsResponse;
import com.deutschlingodeck.statistics.dto.LearningHistoryResponse;
import com.deutschlingodeck.statistics.dto.MasteryBreakdownResponse;
import com.deutschlingodeck.statistics.service.StatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(ApiConstants.API_BASE_PATH + "/statistics")
public class StatisticsController {

	private final StatisticsService statisticsService;

	public StatisticsController(StatisticsService statisticsService) {
		this.statisticsService = statisticsService;
	}

	@GetMapping
	public ResponseEntity<DashboardStatisticsResponse> getDashboardStatistics(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(required = false) Long dictionaryId,
			@RequestParam(required = false) String gameMode) {
		return ResponseEntity.ok(statisticsService.getDashboardStatistics(from, to, dictionaryId, gameMode));
	}

	@GetMapping("/cards")
	public ResponseEntity<PageResponse<CardStatisticsResponse>> getCardStatistics(
			@RequestParam(required = false) Long dictionaryId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE) int page,
			@RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE_SIZE) int size) {
		return ResponseEntity.ok(statisticsService.getCardStatistics(dictionaryId, from, to, page, size));
	}

	@GetMapping("/dictionaries")
	public ResponseEntity<List<DictionaryStatisticsResponse>> getDictionaryStatistics() {
		return ResponseEntity.ok(statisticsService.getDictionaryStatistics());
	}

	@GetMapping("/mastery")
	public ResponseEntity<MasteryBreakdownResponse> getMasteryBreakdown(
			@RequestParam(required = false) Long dictionaryId) {
		return ResponseEntity.ok(statisticsService.getMasteryBreakdown(dictionaryId));
	}

	@GetMapping("/history")
	public ResponseEntity<PageResponse<LearningHistoryResponse>> getLearningHistory(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(required = false) Long dictionaryId,
			@RequestParam(required = false) String gameMode,
			@RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE) int page,
			@RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE_SIZE) int size) {
		return ResponseEntity.ok(statisticsService.getLearningHistory(from, to, dictionaryId, gameMode, page, size));
	}
}
