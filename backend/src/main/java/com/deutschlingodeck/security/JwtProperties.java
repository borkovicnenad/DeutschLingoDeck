package com.deutschlingodeck.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** Binds the {@code security.jwt.*} keys in application.yaml. */
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(String secret, Duration accessTokenTtl, Duration refreshTokenTtl) {
}
