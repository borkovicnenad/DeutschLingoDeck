package com.deutschlingodeck.common.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;

/**
 * Project-wide Jackson (v3 / {@code tools.jackson}) customizations layered
 * on top of Spring Boot's default auto-configuration (which already
 * registers JSR-310 support and writes dates in ISO-8601 form).
 */
@Configuration
public class JacksonConfig {

	@Bean
	public JsonMapperBuilderCustomizer jsonMapperBuilderCustomizer() {
		return builder -> builder.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}
}
