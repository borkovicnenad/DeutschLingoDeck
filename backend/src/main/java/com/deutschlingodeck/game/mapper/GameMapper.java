package com.deutschlingodeck.game.mapper;

import com.deutschlingodeck.common.mapper.MapStructConfig;
import com.deutschlingodeck.dictionary.entity.Card;
import com.deutschlingodeck.game.dto.CurrentCardResponse;
import com.deutschlingodeck.game.dto.GameResponse;
import com.deutschlingodeck.game.dto.GameSummaryResponse;
import com.deutschlingodeck.game.entity.Game;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * {@code currentCard} (on {@code GameResponse}) and {@code accuracy} /
 * {@code durationSeconds} / {@code averageResponseTimeMs} (on
 * {@code GameSummaryResponse}) are derived values with no matching entity
 * field; the service layer computes and sets them once the game engine is
 * implemented.
 */
@Mapper(config = MapStructConfig.class)
public interface GameMapper {

	@Mapping(target = "dictionaryId", source = "dictionary.id")
	@Mapping(target = "currentCard", ignore = true)
	GameResponse toGameResponse(Game game);

	CurrentCardResponse toCurrentCardResponse(Card card);

	@Mapping(target = "gameId", source = "id")
	@Mapping(target = "accuracy", ignore = true)
	@Mapping(target = "durationSeconds", ignore = true)
	@Mapping(target = "averageResponseTimeMs", ignore = true)
	GameSummaryResponse toGameSummaryResponse(Game game);
}
