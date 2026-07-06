package com.deutschlingodeck.dictionary.service;

import com.deutschlingodeck.authentication.entity.User;
import com.deutschlingodeck.authentication.repository.UserRepository;
import com.deutschlingodeck.dictionary.dto.CardType;
import com.deutschlingodeck.dictionary.dto.DictionaryImportRequest;
import com.deutschlingodeck.dictionary.dto.DictionaryImportResponse;
import com.deutschlingodeck.dictionary.entity.Card;
import com.deutschlingodeck.dictionary.entity.Dictionary;
import com.deutschlingodeck.dictionary.exception.DictionaryImportException;
import com.deutschlingodeck.dictionary.repository.CardRepository;
import com.deutschlingodeck.dictionary.repository.DictionaryRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Parses an uploaded {@code .xlsx}/{@code .xls}/{@code .csv} vocabulary file into
 * {@link Card}s. Both formats are normalized into the same {@code List<Map<String,String>>}
 * row shape (keyed by canonical field name) so row validation only has to be written once.
 */
@Service
public class DictionaryImportServiceImpl implements DictionaryImportService {

	private static final Map<String, String> HEADER_SYNONYMS = Map.ofEntries(
			Map.entry("germanword", "sourceText"),
			Map.entry("word", "sourceText"),
			Map.entry("source", "sourceText"),
			Map.entry("sourcetext", "sourceText"),
			Map.entry("translation", "primaryTranslation"),
			Map.entry("meaning", "primaryTranslation"),
			Map.entry("examplesentence", "example"),
			Map.entry("example", "example"),
			Map.entry("sentence", "example"),
			Map.entry("difficulty", "difficultyLevel"),
			Map.entry("difficultylevel", "difficultyLevel"),
			Map.entry("level", "difficultyLevel"),
			Map.entry("tags", "tags"),
			Map.entry("tag", "tags"),
			Map.entry("notes", "notes"),
			Map.entry("note", "notes"),
			Map.entry("article", "article"),
			Map.entry("type", "cardType"),
			Map.entry("cardtype", "cardType")
	);

	private final DictionaryRepository dictionaryRepository;
	private final CardRepository cardRepository;
	private final UserRepository userRepository;

	public DictionaryImportServiceImpl(
			DictionaryRepository dictionaryRepository, CardRepository cardRepository, UserRepository userRepository) {
		this.dictionaryRepository = dictionaryRepository;
		this.cardRepository = cardRepository;
		this.userRepository = userRepository;
	}

	@Override
	public DictionaryImportResponse importDictionary(Long userId, DictionaryImportRequest request) {
		MultipartFile file = request.file();
		String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase(Locale.ROOT) : "";

		List<Map<String, String>> rows;
		try {
			if (filename.endsWith(".csv")) {
				rows = parseCsv(file.getInputStream());
			} else if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
				rows = parseWorkbook(file.getInputStream());
			} else {
				throw new DictionaryImportException("Unsupported file type - please upload a .xlsx, .xls or .csv file");
			}
		} catch (IOException | RuntimeException ex) {
			if (ex instanceof DictionaryImportException importException) {
				throw importException;
			}
			throw new DictionaryImportException("The uploaded file could not be read: " + ex.getMessage());
		}

		if (rows.isEmpty()) {
			throw new DictionaryImportException("The uploaded file does not contain any data rows");
		}

		List<String> warnings = new ArrayList<>();
		List<Card> cards = buildCards(rows, warnings);

		if (cards.isEmpty()) {
			throw new DictionaryImportException("No valid vocabulary rows were found in the uploaded file");
		}

		User owner = userRepository.getReferenceById(userId);
		Dictionary dictionary = new Dictionary(owner, request.name(), request.sourceLanguage(), request.targetLanguage());
		dictionary.setDescription(request.description());
		dictionary.setCreatedAt(OffsetDateTime.now());
		dictionary = dictionaryRepository.save(dictionary);

		int position = 0;
		for (Card card : cards) {
			card.setDictionary(dictionary);
			card.setPosition(position++);
		}
		cardRepository.saveAll(cards);

		return new DictionaryImportResponse(dictionary.getId(), dictionary.getName(), cards.size(), "IMPORTED", warnings);
	}

	private List<Card> buildCards(List<Map<String, String>> rows, List<String> warnings) {
		List<Card> cards = new ArrayList<>();
		int rowNumber = 1; // row 1 is the header
		for (Map<String, String> row : rows) {
			rowNumber++;
			String sourceText = trimToNull(row.get("sourceText"));
			if (sourceText == null) {
				warnings.add("Row " + rowNumber + ": skipped, missing the German word/source text");
				continue;
			}

			Card card = new Card(null, parseCardType(row.get("cardType"), sourceText), sourceText, 0);
			card.setPrimaryTranslation(trimToNull(row.get("primaryTranslation")));
			card.setArticle(trimToNull(row.get("article")));
			card.setExample(trimToNull(row.get("example")));
			card.setNotes(trimToNull(row.get("notes")));

			String translation = trimToNull(row.get("primaryTranslation"));
			if (translation != null) {
				card.setAcceptedAnswers(List.of(translation));
			}

			applyDifficulty(card, row.get("difficultyLevel"), rowNumber, warnings);
			applyTags(card, row.get("tags"));

			cards.add(card);
		}
		return cards;
	}

	private void applyDifficulty(Card card, String raw, int rowNumber, List<String> warnings) {
		String difficultyRaw = trimToNull(raw);
		if (difficultyRaw == null) {
			return;
		}
		try {
			int difficulty = Integer.parseInt(difficultyRaw);
			if (difficulty >= 1 && difficulty <= 5) {
				card.setDifficultyLevel(difficulty);
			} else {
				warnings.add("Row " + rowNumber + ": difficulty '" + difficultyRaw + "' is out of range (1-5), ignored");
			}
		} catch (NumberFormatException ex) {
			warnings.add("Row " + rowNumber + ": difficulty '" + difficultyRaw + "' is not a number, ignored");
		}
	}

	private void applyTags(Card card, String raw) {
		String tagsRaw = trimToNull(raw);
		if (tagsRaw == null) {
			return;
		}
		List<String> tags = List.of(tagsRaw.split("[,;]")).stream()
				.map(String::trim)
				.filter(tag -> !tag.isEmpty())
				.toList();
		if (!tags.isEmpty()) {
			card.setTags(tags);
		}
	}

	private CardType parseCardType(String raw, String sourceText) {
		String trimmed = trimToNull(raw);
		if (trimmed != null) {
			try {
				return CardType.valueOf(trimmed.toUpperCase(Locale.ROOT));
			} catch (IllegalArgumentException ignored) {
				// fall through to inference below
			}
		}
		return sourceText.contains(" ") ? CardType.PHRASE : CardType.WORD;
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private List<Map<String, String>> parseWorkbook(InputStream inputStream) throws IOException {
		try (Workbook workbook = WorkbookFactory.create(inputStream)) {
			Sheet sheet = workbook.getSheetAt(0);
			DataFormatter formatter = new DataFormatter();

			Row headerRow = sheet.getRow(sheet.getFirstRowNum());
			if (headerRow == null) {
				return List.of();
			}

			List<String> headers = new ArrayList<>();
			for (Cell cell : headerRow) {
				headers.add(normalizeHeader(formatter.formatCellValue(cell)));
			}

			List<Map<String, String>> rows = new ArrayList<>();
			for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
				Row row = sheet.getRow(r);
				if (row == null) {
					continue;
				}
				Map<String, String> values = new LinkedHashMap<>();
				boolean hasContent = false;
				for (int c = 0; c < headers.size(); c++) {
					Cell cell = row.getCell(c);
					String value = cell == null ? "" : formatter.formatCellValue(cell);
					if (!value.isBlank()) {
						hasContent = true;
					}
					values.put(headers.get(c), value);
				}
				if (hasContent) {
					rows.add(values);
				}
			}
			return rows;
		}
	}

	private List<Map<String, String>> parseCsv(InputStream inputStream) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			String headerLine = reader.readLine();
			if (headerLine == null) {
				return List.of();
			}
			List<String> headers = splitCsvLine(headerLine).stream().map(this::normalizeHeader).toList();

			List<Map<String, String>> rows = new ArrayList<>();
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isBlank()) {
					continue;
				}
				List<String> values = splitCsvLine(line);
				Map<String, String> row = new LinkedHashMap<>();
				for (int i = 0; i < headers.size(); i++) {
					row.put(headers.get(i), i < values.size() ? values.get(i) : "");
				}
				rows.add(row);
			}
			return rows;
		}
	}

	/** Minimal CSV field splitter: comma-separated, double-quoted fields with {@code ""} escaping. */
	private List<String> splitCsvLine(String line) {
		List<String> fields = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		boolean inQuotes = false;
		for (int i = 0; i < line.length(); i++) {
			char ch = line.charAt(i);
			if (inQuotes) {
				if (ch == '"') {
					if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
						current.append('"');
						i++;
					} else {
						inQuotes = false;
					}
				} else {
					current.append(ch);
				}
			} else if (ch == '"') {
				inQuotes = true;
			} else if (ch == ',') {
				fields.add(current.toString());
				current.setLength(0);
			} else {
				current.append(ch);
			}
		}
		fields.add(current.toString());
		return fields;
	}

	private String normalizeHeader(String header) {
		String key = header.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
		return HEADER_SYNONYMS.getOrDefault(key, key);
	}
}
