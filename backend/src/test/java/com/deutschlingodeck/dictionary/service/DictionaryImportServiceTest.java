package com.deutschlingodeck.dictionary.service;

import com.deutschlingodeck.authentication.entity.User;
import com.deutschlingodeck.authentication.repository.UserRepository;
import com.deutschlingodeck.dictionary.dto.DictionaryImportRequest;
import com.deutschlingodeck.dictionary.dto.DictionaryImportResponse;
import com.deutschlingodeck.dictionary.entity.Dictionary;
import com.deutschlingodeck.dictionary.exception.DictionaryImportException;
import com.deutschlingodeck.dictionary.repository.CardRepository;
import com.deutschlingodeck.dictionary.repository.DictionaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DictionaryImportServiceTest {

	@Mock
	private DictionaryRepository dictionaryRepository;
	@Mock
	private CardRepository cardRepository;
	@Mock
	private UserRepository userRepository;

	private DictionaryImportServiceImpl importService;

	@BeforeEach
	void setUp() {
		importService = new DictionaryImportServiceImpl(dictionaryRepository, cardRepository, userRepository);
	}

	@Test
	void importDictionary_parsesValidRowsAndSkipsInvalidOnesWithWarnings() {
		User user = new User("user@example.com", "hash", "User");
		ReflectionTestUtils.setField(user, "id", 1L);
		when(userRepository.getReferenceById(1L)).thenReturn(user);
		when(dictionaryRepository.save(any(Dictionary.class))).thenAnswer(invocation -> {
			Dictionary dictionary = invocation.getArgument(0);
			ReflectionTestUtils.setField(dictionary, "id", 10L);
			return dictionary;
		});
		when(cardRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

		String csv = """
				Word,Translation,Example,Difficulty,Tags
				Hund,dog,Der Hund bellt.,2,animals
				,cat,,3,animals
				Katze,cat,,high,animals;pets
				""";
		MockMultipartFile file = new MockMultipartFile("file", "vocab.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));
		DictionaryImportRequest request = new DictionaryImportRequest(file, "Animals", null, "hr", "de");

		DictionaryImportResponse response = importService.importDictionary(1L, request);

		assertThat(response.importedCards()).isEqualTo(2);
		assertThat(response.status()).isEqualTo("IMPORTED");
		assertThat(response.warnings()).hasSize(2);
		assertThat(response.warnings().get(0)).contains("missing the German word");
		assertThat(response.warnings().get(1)).contains("not a number");
	}

	@Test
	void importDictionary_rejectsUnsupportedFileType() {
		MockMultipartFile file = new MockMultipartFile("file", "vocab.pdf", "application/pdf", new byte[] {1, 2, 3});
		DictionaryImportRequest request = new DictionaryImportRequest(file, "Animals", null, "hr", "de");

		assertThatThrownBy(() -> importService.importDictionary(1L, request)).isInstanceOf(DictionaryImportException.class);
	}

	@Test
	void importDictionary_rejectsFileWithNoValidRows() {
		String csv = "Word,Translation\n,cat\n";
		MockMultipartFile file = new MockMultipartFile("file", "vocab.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));
		DictionaryImportRequest request = new DictionaryImportRequest(file, "Animals", null, "hr", "de");

		assertThatThrownBy(() -> importService.importDictionary(1L, request)).isInstanceOf(DictionaryImportException.class);
	}
}
