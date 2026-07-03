package com.deutschlingodeck.dictionary.mapper;

import com.deutschlingodeck.common.mapper.MapStructConfig;
import com.deutschlingodeck.dictionary.dto.CardDetailResponse;
import com.deutschlingodeck.dictionary.dto.CardSummaryResponse;
import com.deutschlingodeck.dictionary.entity.Card;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface CardMapper {

	CardSummaryResponse toSummaryResponse(Card card);

	CardDetailResponse toDetailResponse(Card card);
}
