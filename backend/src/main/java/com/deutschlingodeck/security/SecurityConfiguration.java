package com.deutschlingodeck.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for the API: stateless JWT bearer authentication.
 *
 * <p>{@link JwtAuthenticationFilter} runs before Spring Security's own
 * authentication filter and populates the context from the
 * {@code Authorization} header; {@link SecurityConstants#PUBLIC_ENDPOINTS}
 * stays open (registration/login/refresh/docs/health), everything else
 * requires a valid access token. CSRF is disabled and the session is
 * stateless since this is a token-based API, not a browser session-based one.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfiguration {

	private final RestAuthenticationEntryPoint authenticationEntryPoint;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SecurityConfiguration(
			RestAuthenticationEntryPoint authenticationEntryPoint,
			JwtAuthenticationFilter jwtAuthenticationFilter) {
		this.authenticationEntryPoint = authenticationEntryPoint;
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(authenticationEntryPoint))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(SecurityConstants.PUBLIC_ENDPOINTS).permitAll()
						.anyRequest().authenticated()
				)
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
