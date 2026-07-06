package com.deutschlingodeck.dictionary.service;

import com.deutschlingodeck.authentication.repository.UserRepository;
import com.deutschlingodeck.common.exception.ResourceNotFoundException;
import com.deutschlingodeck.common.pagination.PageMapper;
import com.deutschlingodeck.common.pagination.PageResponse;
import com.deutschlingodeck.common.security.CurrentUserProvider;
import com.deutschlingodeck.common.security.OwnershipGuard;
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
import com.deutschlingodeck.dictionary.entity.Card;
import com.deutschlingodeck.dictionary.entity.Dictionary;
import com.deutschlingodeck.dictionary.mapper.CardMapper;
import com.deutschlingodeck.dictionary.mapper.DictionaryMapper;
import com.deutschlingodeck.dictionary.repository.CardRepository;
import com.deutschlingodeck.dictionary.repository.DictionaryRepository;
import com.deutschlingodeck.game.progress.CardProgress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class DictionaryServiceImpl implements DictionaryService {

	private final DictionaryRepository dictionaryRepository;
	private final CardRepository cardRepository;
	private final DictionaryMapper dictionaryMapper;
	private final CardMapper cardMapper;
	private final CurrentUserProvider currentUserProvider;
	private final OwnershipGuard ownershipGuard;
	private final DictionaryImportService dictionaryImportService;
	private final UserRepository userRepository;

	public DictionaryServiceImpl(
			DictionaryRepository dictionaryRepository,
			CardRepository cardRepository,
			DictionaryMapper dictionaryMapper,
			CardMapper cardMapper,
			CurrentUserProvider currentUserProvider,
			OwnershipGuard ownershipGuard,
			DictionaryImportService dictionaryImportService,
			UserRepository userRepository) {
		this.dictionaryRepository = dictionaryRepository;
		this.cardRepository = cardRepository;
		this.dictionaryMapper = dictionaryMapper;
		this.cardMapper = cardMapper;
		this.currentUserProvider = currentUserProvider;
		this.ownershipGuard = ownershipGuard;
		this.dictionaryImportService = dictionaryImportService;
		this.userRepository = userRepository;
	}

	@Override
	public PageResponse<DictionarySummaryResponse> listDictionaries(int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<Dictionary> dictionaries = dictionaryRepository.findByOwnerIdAndDeletedFalse(currentUserProvider.getUserId(), pageable);
		return PageMapper.toPageResponse(dictionaries, this::toSummaryWithCardCount);
	}

	@Override
	@Transactional
	public DictionaryDetailResponse createDictionary(CreateDictionaryRequest request) {
		Dictionary dictionary = new Dictionary(
				userRepository.getReferenceById(currentUserProvider.getUserId()),
				request.name(), request.sourceLanguage(), request.targetLanguage());
		dictionary.setDescription(request.description());
		dictionary.setCreatedAt(OffsetDateTime.now());
		return toDetailWithCardCount(dictionaryRepository.save(dictionary));
	}

	@Override
	@Transactional
	public DictionaryImportResponse importDictionary(DictionaryImportRequest request) {
		return dictionaryImportService.importDictionary(currentUserProvider.getUserId(), request);
	}

	@Override
	public DictionaryDetailResponse getDictionary(Long dictionaryId) {
		return toDetailWithCardCount(loadOwnedDictionary(dictionaryId));
	}

	@Override
	@Transactional
	public DictionaryDetailResponse updateDictionary(Long dictionaryId, UpdateDictionaryRequest request) {
		Dictionary dictionary = loadOwnedDictionary(dictionaryId);
		dictionary.setName(request.name());
		dictionary.setDescription(request.description());
		dictionary.setSourceLanguage(request.sourceLanguage());
		dictionary.setTargetLanguage(request.targetLanguage());
		dictionary.setUpdatedAt(OffsetDateTime.now());
		return toDetailWithCardCount(dictionary);
	}

	@Override
	@Transactional
	public void deleteDictionary(Long dictionaryId) {
		Dictionary dictionary = loadOwnedDictionary(dictionaryId);
		dictionary.setDeleted(true);
		dictionary.setUpdatedAt(OffsetDateTime.now());
	}

	@Override
	public PageResponse<CardSummaryResponse> listCards(
			Long dictionaryId, String tag, Integer difficulty, String status, String search, int page, int size) {
		loadOwnedDictionary(dictionaryId);
		Pageable pageable = PageRequest.of(page, size);
		Page<Card> cards = cardRepository.search(
				dictionaryId, currentUserProvider.getUserId(), tag, difficulty, status, search,
				CardProgress.MASTERED_INTERVAL_DAYS_THRESHOLD, pageable);
		return PageMapper.toPageResponse(cards, cardMapper::toSummaryResponse);
	}

	@Override
	public CardDetailResponse getCard(Long dictionaryId, Long cardId) {
		loadOwnedDictionary(dictionaryId);
		return cardMapper.toDetailResponse(loadCard(dictionaryId, cardId));
	}

	@Override
	@Transactional
	public CardDetailResponse createCard(Long dictionaryId, CreateCardRequest request) {
		Dictionary dictionary = loadOwnedDictionary(dictionaryId);
		int nextPosition = cardRepository.findMaxPositionByDictionaryId(dictionaryId) + 1;

		Card card = new Card(dictionary, request.cardType(), request.sourceText(), nextPosition);
		card.setArticle(request.article());
		card.setPrimaryTranslation(request.primaryTranslation());
		card.setAcceptedAnswers(request.acceptedAnswers());
		card.setExample(request.example());
		card.setNotes(request.notes());
		card.setDifficultyLevel(request.difficultyLevel());
		card.setTags(request.tags());

		return cardMapper.toDetailResponse(cardRepository.save(card));
	}

	@Override
	@Transactional
	public CardDetailResponse updateCard(Long dictionaryId, Long cardId, UpdateCardRequest request) {
		loadOwnedDictionary(dictionaryId);
		Card card = loadCard(dictionaryId, cardId);

		card.setCardType(request.cardType());
		card.setArticle(request.article());
		card.setSourceText(request.sourceText());
		card.setPrimaryTranslation(request.primaryTranslation());
		card.setAcceptedAnswers(request.acceptedAnswers());
		card.setExample(request.example());
		card.setNotes(request.notes());
		card.setDifficultyLevel(request.difficultyLevel());
		card.setTags(request.tags());

		return cardMapper.toDetailResponse(card);
	}

	@Override
	@Transactional
	public void deleteCard(Long dictionaryId, Long cardId) {
		loadOwnedDictionary(dictionaryId);
		cardRepository.delete(loadCard(dictionaryId, cardId));
	}

	private Dictionary loadOwnedDictionary(Long dictionaryId) {
		Dictionary dictionary = dictionaryRepository.findById(dictionaryId)
				.filter(d -> !d.isDeleted())
				.orElseThrow(() -> ResourceNotFoundException.of("Dictionary", dictionaryId));
		ownershipGuard.requireOwner(dictionary.getOwner().getId(), currentUserProvider.getUserId());
		return dictionary;
	}

	private Card loadCard(Long dictionaryId, Long cardId) {
		return cardRepository.findByIdAndDictionaryId(cardId, dictionaryId)
				.orElseThrow(() -> ResourceNotFoundException.of("Card", cardId));
	}

	private DictionarySummaryResponse toSummaryWithCardCount(Dictionary dictionary) {
		DictionarySummaryResponse base = dictionaryMapper.toSummaryResponse(dictionary);
		return new DictionarySummaryResponse(
				base.id(), base.name(), base.description(), base.sourceLanguage(), base.targetLanguage(),
				cardRepository.countByDictionaryId(dictionary.getId()), base.createdAt());
	}

	private DictionaryDetailResponse toDetailWithCardCount(Dictionary dictionary) {
		DictionaryDetailResponse base = dictionaryMapper.toDetailResponse(dictionary);
		return new DictionaryDetailResponse(
				base.id(), base.name(), base.description(), base.sourceLanguage(), base.targetLanguage(),
				cardRepository.countByDictionaryId(dictionary.getId()), base.createdAt(), base.updatedAt());
	}
}
