package com.deutschlingodeck.dictionary.service;

import com.deutschlingodeck.dictionary.dto.DictionaryImportRequest;
import com.deutschlingodeck.dictionary.dto.DictionaryImportResponse;

public interface DictionaryImportService {

	DictionaryImportResponse importDictionary(Long userId, DictionaryImportRequest request);
}
