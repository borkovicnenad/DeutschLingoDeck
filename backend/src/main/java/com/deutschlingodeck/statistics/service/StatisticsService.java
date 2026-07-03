package com.deutschlingodeck.statistics.service;

import com.deutschlingodeck.common.pagination.PageResponse;
import com.deutschlingodeck.statistics.dto.CardStatisticsResponse;
import com.deutschlingodeck.statistics.dto.DashboardStatisticsResponse;
import com.deutschlingodeck.statistics.dto.DictionaryStatisticsResponse;
import com.deutschlingodeck.statistics.dto.LearningHistoryResponse;

import java.time.LocalDate;
import java.util.List;

public interface StatisticsService {

	DashboardStatisticsResponse getDashboardStatistics(LocalDate from, LocalDate to);

	PageResponse<CardStatisticsResponse> getCardStatistics(Long dictionaryId, int page, int size);

	List<DictionaryStatisticsResponse> getDictionaryStatistics();

	PageResponse<LearningHistoryResponse> getLearningHistory(LocalDate from, LocalDate to, int page, int size);
}
