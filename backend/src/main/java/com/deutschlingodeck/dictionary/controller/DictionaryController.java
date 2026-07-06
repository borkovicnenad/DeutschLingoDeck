package com.deutschlingodeck.dictionary.controller;

import com.deutschlingodeck.common.constants.ApiConstants;
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
import com.deutschlingodeck.dictionary.service.DictionaryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.API_BASE_PATH + "/dictionaries")
public class DictionaryController {

	private final DictionaryService dictionaryService;

	public DictionaryController(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	@GetMapping
	public ResponseEntity<PageResponse<DictionarySummaryResponse>> listDictionaries(
			@RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE) int page,
			@RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE_SIZE) int size) {
		return ResponseEntity.ok(dictionaryService.listDictionaries(page, size));
	}

	@PostMapping
	public ResponseEntity<DictionaryDetailResponse> createDictionary(@Valid @RequestBody CreateDictionaryRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(dictionaryService.createDictionary(request));
	}

	@PostMapping(value = "/import", consumes = "multipart/form-data")
	public ResponseEntity<DictionaryImportResponse> importDictionary(@Valid @ModelAttribute DictionaryImportRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(dictionaryService.importDictionary(request));
	}

	@GetMapping("/{dictionaryId}")
	public ResponseEntity<DictionaryDetailResponse> getDictionary(@PathVariable Long dictionaryId) {
		return ResponseEntity.ok(dictionaryService.getDictionary(dictionaryId));
	}

	@PutMapping("/{dictionaryId}")
	public ResponseEntity<DictionaryDetailResponse> updateDictionary(
			@PathVariable Long dictionaryId, @Valid @RequestBody UpdateDictionaryRequest request) {
		return ResponseEntity.ok(dictionaryService.updateDictionary(dictionaryId, request));
	}

	@DeleteMapping("/{dictionaryId}")
	public ResponseEntity<Void> deleteDictionary(@PathVariable Long dictionaryId) {
		dictionaryService.deleteDictionary(dictionaryId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{dictionaryId}/cards")
	public ResponseEntity<PageResponse<CardSummaryResponse>> listCards(
			@PathVariable Long dictionaryId,
			@RequestParam(required = false) String tag,
			@RequestParam(required = false) Integer difficulty,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String search,
			@RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE) int page,
			@RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE_SIZE) int size) {
		return ResponseEntity.ok(dictionaryService.listCards(dictionaryId, tag, difficulty, status, search, page, size));
	}

	@GetMapping("/{dictionaryId}/cards/{cardId}")
	public ResponseEntity<CardDetailResponse> getCard(@PathVariable Long dictionaryId, @PathVariable Long cardId) {
		return ResponseEntity.ok(dictionaryService.getCard(dictionaryId, cardId));
	}

	@PostMapping("/{dictionaryId}/cards")
	public ResponseEntity<CardDetailResponse> createCard(
			@PathVariable Long dictionaryId, @Valid @RequestBody CreateCardRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(dictionaryService.createCard(dictionaryId, request));
	}

	@PutMapping("/{dictionaryId}/cards/{cardId}")
	public ResponseEntity<CardDetailResponse> updateCard(
			@PathVariable Long dictionaryId, @PathVariable Long cardId, @Valid @RequestBody UpdateCardRequest request) {
		return ResponseEntity.ok(dictionaryService.updateCard(dictionaryId, cardId, request));
	}

	@DeleteMapping("/{dictionaryId}/cards/{cardId}")
	public ResponseEntity<Void> deleteCard(@PathVariable Long dictionaryId, @PathVariable Long cardId) {
		dictionaryService.deleteCard(dictionaryId, cardId);
		return ResponseEntity.noContent().build();
	}
}
