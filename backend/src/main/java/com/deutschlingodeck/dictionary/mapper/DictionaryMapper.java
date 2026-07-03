package com.deutschlingodeck.dictionary.mapper;

import com.deutschlingodeck.common.mapper.MapStructConfig;
import com.deutschlingodeck.dictionary.dto.DictionaryDetailResponse;
import com.deutschlingodeck.dictionary.dto.DictionarySummaryResponse;
import com.deutschlingodeck.dictionary.entity.Dictionary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * {@code cardCount} has no matching entity field (it's a derived count) and
 * is intentionally left unmapped here; the service layer is responsible for
 * enriching it once the card-counting logic is implemented.
 */
@Mapper(config = MapStructConfig.class)
public interface DictionaryMapper {

	@Mapping(target = "cardCount", ignore = true)
	DictionarySummaryResponse toSummaryResponse(Dictionary dictionary);

	@Mapping(target = "cardCount", ignore = true)
	DictionaryDetailResponse toDetailResponse(Dictionary dictionary);
}
