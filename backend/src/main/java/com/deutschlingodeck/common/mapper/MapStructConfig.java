package com.deutschlingodeck.common.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * Shared MapStruct configuration applied to every mapper in the project via
 * {@code @Mapper(config = MapStructConfig.class)}.
 */
@MapperConfig(
		componentModel = "spring",
		unmappedTargetPolicy = ReportingPolicy.IGNORE,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface MapStructConfig {
}
