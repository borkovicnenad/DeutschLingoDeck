package com.deutschlingodeck.statistics.service;

import com.deutschlingodeck.common.pagination.PageResponse;
import com.deutschlingodeck.statistics.dto.CardStatisticsResponse;
import com.deutschlingodeck.statistics.dto.DashboardStatisticsResponse;
import com.deutschlingodeck.statistics.dto.DictionaryStatisticsResponse;
import com.deutschlingodeck.statistics.dto.LearningHistoryResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class StatisticsServiceImpl implements StatisticsService {

	@Override
	public DashboardStatisticsResponse getDashboardStatistics(LocalDate from, LocalDate to) {
		// TODO: aggregate game/answer statistics for the current user within [from, to].
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public PageResponse<CardStatisticsResponse> getCardStatistics(Long dictionaryId, int page, int size) {
		// TODO: page per-card statistics, optionally filtered by dictionaryId.
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public List<DictionaryStatisticsResponse> getDictionaryStatistics() {
		// TODO: aggregate per-dictionary statistics for the current user.
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public PageResponse<LearningHistoryResponse> getLearningHistory(LocalDate from, LocalDate to, int page, int size) {
		// TODO: page the current user's game history within [from, to].
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
