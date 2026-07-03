package com.deutschlingodeck.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Springdoc/Swagger UI metadata. This only affects how the
 * auto-generated documentation is presented; the actual API contract lives
 * in {@code docs/openapi.yaml}.
 */
@Configuration
public class OpenApiConfig {

	private static final String BEARER_SCHEME_NAME = "bearerAuth";

	@Bean
	public OpenAPI deutschLingoDeckOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("DeutschLingoDeck Backend API")
						.version("1.0.0")
						.description("REST API for DeutschLingoDeck, a German vocabulary learning platform."))
				.components(new Components()
						.addSecuritySchemes(BEARER_SCHEME_NAME, new SecurityScheme()
								.name(BEARER_SCHEME_NAME)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")));
	}
}
