package com.deutschlingodeck.dictionary.service;

import com.deutschlingodeck.common.pagination.PageResponse;
import com.deutschlingodeck.dictionary.dto.CardDetailResponse;
import com.deutschlingodeck.dictionary.dto.CardSummaryResponse;
import com.deutschlingodeck.dictionary.dto.CreateCardRequest;
import com.deutschlingodeck.dictionary.dto.CreateDictionaryRequest;
import com.deutschlingodeck.dictionary.dto.DictionaryDetailResponse;
import com.deutschlingodeck.dictionary.dto.DictionaryImportRequest;
import com.deutschlingodeck.dictionary.dto.DictionaryImportResponse;
import com.deutschlingodeck.dictionary.dto.DictionarySummaryResponse;
import com.deutschlingodeck.dictionary.dto.UpdateCardRequest;
import com.deutschlingodeck.dictionary.dto.UpdateDictionaryRequest;

public interface DictionaryService {

	PageResponse<DictionarySummaryResponse> listDictionaries(int page, int size);

	DictionaryDetailResponse createDictionary(CreateDictionaryRequest request);

	DictionaryImportResponse importDictionary(DictionaryImportRequest request);

	DictionaryDetailResponse getDictionary(Long dictionaryId);

	DictionaryDetailResponse updateDictionary(Long dictionaryId, UpdateDictionaryRequest request);

	void deleteDictionary(Long dictionaryId);

	PageResponse<CardSummaryResponse> listCards(
			Long dictionaryId, String tag, Integer difficulty, String status, String search, int page, int size);

	CardDetailResponse getCard(Long dictionaryId, Long cardId);

	CardDetailResponse createCard(Long dictionaryId, CreateCardRequest request);

	CardDetailResponse updateCard(Long dictionaryId, Long cardId, UpdateCardRequest request);

	void deleteCard(Long dictionaryId, Long cardId);
}
