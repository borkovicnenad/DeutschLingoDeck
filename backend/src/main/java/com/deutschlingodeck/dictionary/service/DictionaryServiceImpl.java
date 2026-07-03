package com.deutschlingodeck.dictionary.service;

import com.deutschlingodeck.common.pagination.PageResponse;
import com.deutschlingodeck.dictionary.dto.CardDetailResponse;
import com.deutschlingodeck.dictionary.dto.CardSummaryResponse;
import com.deutschlingodeck.dictionary.dto.DictionaryDetailResponse;
import com.deutschlingodeck.dictionary.dto.DictionaryImportRequest;
import com.deutschlingodeck.dictionary.dto.DictionaryImportResponse;
import com.deutschlingodeck.dictionary.dto.DictionarySummaryResponse;
import com.deutschlingodeck.dictionary.dto.UpdateDictionaryRequest;
import com.deutschlingodeck.dictionary.repository.CardRepository;
import com.deutschlingodeck.dictionary.repository.DictionaryRepository;
import org.springframework.stereotype.Service;

@Service
public class DictionaryServiceImpl implements DictionaryService {

	private final DictionaryRepository dictionaryRepository;
	private final CardRepository cardRepository;

	public DictionaryServiceImpl(DictionaryRepository dictionaryRepository, CardRepository cardRepository) {
		this.dictionaryRepository = dictionaryRepository;
		this.cardRepository = cardRepository;
	}

	@Override
	public PageResponse<DictionarySummaryResponse> listDictionaries(int page, int size) {
		// TODO: page dictionaries owned by the current user via dictionaryRepository.
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public DictionaryImportResponse importDictionary(DictionaryImportRequest request) {
		// TODO: parse the uploaded Excel file, create the Dictionary and its Cards.
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public DictionaryDetailResponse getDictionary(Long dictionaryId) {
		// TODO: load the dictionary, verify ownership, map to DictionaryDetailResponse.
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public DictionaryDetailResponse updateDictionary(Long dictionaryId, UpdateDictionaryRequest request) {
		// TODO: load the dictionary, verify ownership, apply changes and persist.
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public void deleteDictionary(Long dictionaryId) {
		// TODO: soft-delete the dictionary (mark as deleted rather than removing the row).
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public PageResponse<CardSummaryResponse> listCards(Long dictionaryId, int page, int size) {
		// TODO: page cards belonging to the dictionary via cardRepository.
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public CardDetailResponse getCard(Long dictionaryId, Long cardId) {
		// TODO: load the card, verify it belongs to the dictionary, map to CardDetailResponse.
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
