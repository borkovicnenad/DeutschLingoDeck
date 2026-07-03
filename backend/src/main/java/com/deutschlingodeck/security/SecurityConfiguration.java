package com.deutschlingodeck.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security skeleton for the API.
 *
 * <p>JWT authentication is not implemented yet, so every endpoint is
 * currently permitted. CSRF is disabled and the session is stateless since
 * this is a token-based API, not a browser session-based one.
 *
 * TODO: once JWT authentication is added:
 * <ul>
 *   <li>register a JWT authentication filter before
 *       {@code UsernamePasswordAuthenticationFilter}</li>
 *   <li>replace {@code anyRequest().permitAll()} with
 *       {@code anyRequest().authenticated()}, keeping
 *       {@link SecurityConstants#PUBLIC_ENDPOINTS} open</li>
 *   <li>configure an {@code AuthenticationProvider} backed by the user
 *       repository</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	private final RestAuthenticationEntryPoint authenticationEntryPoint;

	public SecurityConfiguration(RestAuthenticationEntryPoint authenticationEntryPoint) {
		this.authenticationEntryPoint = authenticationEntryPoint;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(authenticationEntryPoint))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(SecurityConstants.PUBLIC_ENDPOINTS).permitAll()
						// TODO: tighten to .anyRequest().authenticated() once JWT auth is in place
						.anyRequest().permitAll()
				);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
