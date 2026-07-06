package com.deutschlingodeck.dictionary.service;

import com.deutschlingodeck.authentication.entity.User;
import com.deutschlingodeck.authentication.repository.UserRepository;
import com.deutschlingodeck.common.exception.ForbiddenException;
import com.deutschlingodeck.common.exception.ResourceNotFoundException;
import com.deutschlingodeck.common.security.CurrentUserProvider;
import com.deutschlingodeck.common.security.OwnershipGuard;
import com.deutschlingodeck.dictionary.dto.DictionaryDetailResponse;
import com.deutschlingodeck.dictionary.entity.Dictionary;
import com.deutschlingodeck.dictionary.mapper.CardMapper;
import com.deutschlingodeck.dictionary.mapper.DictionaryMapper;
import com.deutschlingodeck.dictionary.repository.CardRepository;
import com.deutschlingodeck.dictionary.repository.DictionaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/** Verifies that dictionary ownership is enforced with the real {@link OwnershipGuard}. */
@ExtendWith(MockitoExtension.class)
class DictionaryServiceOwnershipTest {

	@Mock
	private DictionaryRepository dictionaryRepository;
	@Mock
	private CardRepository cardRepository;
	@Mock
	private DictionaryMapper dictionaryMapper;
	@Mock
	private CardMapper cardMapper;
	@Mock
	private CurrentUserProvider currentUserProvider;
	@Mock
	private DictionaryImportService dictionaryImportService;
	@Mock
	private UserRepository userRepository;

	private DictionaryServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new DictionaryServiceImpl(
				dictionaryRepository, cardRepository, dictionaryMapper, cardMapper,
				currentUserProvider, new OwnershipGuard(), dictionaryImportService, userRepository);
	}

	@Test
	void getDictionary_throwsForbidden_whenCurrentUserIsNotTheOwner() {
		User owner = userWithId(2L);
		Dictionary dictionary = new Dictionary(owner, "Travel", "hr", "de");
		ReflectionTestUtils.setField(dictionary, "id", 5L);

		when(currentUserProvider.getUserId()).thenReturn(1L);
		when(dictionaryRepository.findById(5L)).thenReturn(Optional.of(dictionary));

		assertThatThrownBy(() -> service.getDictionary(5L)).isInstanceOf(ForbiddenException.class);
	}

	@Test
	void getDictionary_throwsNotFound_whenDictionaryIsSoftDeleted() {
		User owner = userWithId(1L);
		Dictionary dictionary = new Dictionary(owner, "Travel", "hr", "de");
		ReflectionTestUtils.setField(dictionary, "id", 5L);
		dictionary.setDeleted(true);

		when(dictionaryRepository.findById(5L)).thenReturn(Optional.of(dictionary));

		assertThatThrownBy(() -> service.getDictionary(5L)).isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void getDictionary_succeeds_whenCurrentUserIsTheOwner() {
		User owner = userWithId(1L);
		Dictionary dictionary = new Dictionary(owner, "Travel", "hr", "de");
		ReflectionTestUtils.setField(dictionary, "id", 5L);

		when(currentUserProvider.getUserId()).thenReturn(1L);
		when(dictionaryRepository.findById(5L)).thenReturn(Optional.of(dictionary));
		when(cardRepository.countByDictionaryId(5L)).thenReturn(3);
		when(dictionaryMapper.toDetailResponse(dictionary))
				.thenReturn(new DictionaryDetailResponse(5L, "Travel", null, "hr", "de", null, null, null));

		DictionaryDetailResponse response = service.getDictionary(5L);

		assertThat(response.cardCount()).isEqualTo(3);
	}

	private User userWithId(Long id) {
		User user = new User("user" + id + "@example.com", "hash", "User " + id);
		ReflectionTestUtils.setField(user, "id", id);
		return user;
	}
}
