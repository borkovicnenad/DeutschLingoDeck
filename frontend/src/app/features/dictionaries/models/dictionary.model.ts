export interface DictionarySummary {
  id: number;
  name: string;
  description?: string;
  sourceLanguage: string;
  targetLanguage: string;
  cardCount: number;
  createdAt: string;
}

export interface DictionaryDetail extends DictionarySummary {
  updatedAt?: string;
}

export interface UpdateDictionaryRequest {
  name: string;
  description?: string;
  sourceLanguage: string;
  targetLanguage: string;
}

export interface DictionaryImportRequest {
  file: File;
  name: string;
  description?: string;
  sourceLanguage: string;
  targetLanguage: string;
}

export interface DictionaryImportResponse {
  dictionaryId: number;
  name: string;
  importedCards: number;
  status: string;
  warnings?: string[];
}
