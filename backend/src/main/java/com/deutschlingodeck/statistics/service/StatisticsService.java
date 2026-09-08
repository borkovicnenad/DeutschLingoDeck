package com.deutschlingodeck.statistics.service;

import com.deutschlingodeck.common.pagination.PageResponse;
import com.deutschlingodeck.statistics.dto.CardStatisticsResponse;
import com.deutschlingodeck.statistics.dto.DashboardStatisticsResponse;
import com.deutschlingodeck.statistics.dto.DictionaryStatisticsResponse;
import com.deutschlingodeck.statistics.dto.LearningHistoryResponse;
import com.deutschlingodeck.statistics.dto.MasteryBreakdownResponse;

import java.time.LocalDate;
import java.util.List;

public interface StatisticsService {

	DashboardStatisticsResponse getDashboardStatistics(LocalDate from, LocalDate to, Long dictionaryId, String gameMode);

	PageResponse<CardStatisticsResponse> getCardStatistics(Long dictionaryId, LocalDate from, LocalDate to, int page, int size);

	List<DictionaryStatisticsResponse> getDictionaryStatistics();

	MasteryBreakdownResponse getMasteryBreakdown(Long dictionaryId);

	PageResponse<LearningHistoryResponse> getLearningHistory(
			LocalDate from, LocalDate to, Long dictionaryId, String gameMode, int page, int size);
}
