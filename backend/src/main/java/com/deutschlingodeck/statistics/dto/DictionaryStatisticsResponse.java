package com.deutschlingodeck.statistics.dto;

public record DictionaryStatisticsResponse(
		Long dictionaryId,
		String dictionaryName,
		Integer completedGames,
		Integer totalAnswers,
		Double averageAccuracy,
		Integer masteredCards,
		Integer difficultCards
) {
}
