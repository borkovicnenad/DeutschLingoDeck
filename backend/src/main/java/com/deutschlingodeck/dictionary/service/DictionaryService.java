package com.deutschlingodeck.dictionary.service;

import com.deutschlingodeck.common.pagination.PageResponse;
import com.deutschlingodeck.dictionary.dto.CardDetailResponse;
import com.deutschlingodeck.dictionary.dto.CardSummaryResponse;
import com.deutschlingodeck.dictionary.dto.DictionaryDetailResponse;
import com.deutschlingodeck.dictionary.dto.DictionaryImportRequest;
import com.deutschlingodeck.dictionary.dto.DictionaryImportResponse;
import com.deutschlingodeck.dictionary.dto.DictionarySummaryResponse;
import com.deutschlingodeck.dictionary.dto.UpdateDictionaryRequest;

public interface DictionaryService {

	PageResponse<DictionarySummaryResponse> listDictionaries(int page, int size);

	DictionaryImportResponse importDictionary(DictionaryImportRequest request);

	DictionaryDetailResponse getDictionary(Long dictionaryId);

	DictionaryDetailResponse updateDictionary(Long dictionaryId, UpdateDictionaryRequest request);

	void deleteDictionary(Long dictionaryId);

	PageResponse<CardSummaryResponse> listCards(Long dictionaryId, int page, int size);

	CardDetailResponse getCard(Long dictionaryId, Long cardId);
}
