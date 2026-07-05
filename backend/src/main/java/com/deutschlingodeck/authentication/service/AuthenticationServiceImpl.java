package com.deutschlingodeck.authentication.service;

import com.deutschlingodeck.authentication.dto.AuthResponse;
import com.deutschlingodeck.authentication.dto.LoginRequest;
import com.deutschlingodeck.authentication.dto.RefreshTokenRequest;
import com.deutschlingodeck.authentication.dto.RegisterRequest;
import com.deutschlingodeck.authentication.dto.UserResponse;
import com.deutschlingodeck.authentication.entity.RefreshToken;
import com.deutschlingodeck.authentication.entity.User;
import com.deutschlingodeck.authentication.exception.EmailAlreadyExistsException;
import com.deutschlingodeck.authentication.exception.InvalidCredentialsException;
import com.deutschlingodeck.authentication.exception.InvalidRefreshTokenException;
import com.deutschlingodeck.authentication.mapper.UserMapper;
import com.deutschlingodeck.authentication.repository.RefreshTokenRepository;
import com.deutschlingodeck.authentication.repository.UserRepository;
import com.deutschlingodeck.common.exception.UnauthorizedException;
import com.deutschlingodeck.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final UserMapper userMapper;

	public AuthenticationServiceImpl(
			UserRepository userRepository,
			RefreshTokenRepository refreshTokenRepository,
			PasswordEncoder passwordEncoder,
			JwtTokenProvider jwtTokenProvider,
			UserMapper userMapper) {
		this.userRepository = userRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
		this.userMapper = userMapper;
	}

	@Override
	@Transactional
	public AuthResponse register(RegisterRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new EmailAlreadyExistsException(request.email());
		}

		User user = new User(
				request.email(),
				passwordEncoder.encode(request.password()),
				request.displayName());
		user.setCreatedAt(OffsetDateTime.now());
		user = userRepository.save(user);

		return issueTokens(user);
	}

	@Override
	public AuthResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email())
				.orElseThrow(InvalidCredentialsException::new);

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new InvalidCredentialsException();
		}

		return issueTokens(user);
	}

	@Override
	@Transactional
	public AuthResponse refresh(RefreshTokenRequest request) {
		Claims claims = parseRefreshTokenClaims(request.refreshToken());

		UUID tokenId = UUID.fromString(claims.getId());
		RefreshToken storedToken = refreshTokenRepository.findByTokenId(tokenId)
				.filter(RefreshToken::isValid)
				.orElseThrow(InvalidRefreshTokenException::new);

		// Rotate: the presented refresh token is single-use.
		storedToken.setRevoked(true);
		refreshTokenRepository.save(storedToken);

		User user = userRepository.findById(storedToken.getUserId())
				.orElseThrow(InvalidRefreshTokenException::new);

		return issueTokens(user);
	}

	@Override
	@Transactional
	public void logout() {
		refreshTokenRepository.revokeAllActiveForUser(currentUserId());
	}

	@Override
	public UserResponse getCurrentUser() {
		User user = userRepository.findById(currentUserId())
				.orElseThrow(() -> new UnauthorizedException("Authentication is required"));
		return userMapper.toUserResponse(user);
	}

	/** Issues a fresh access/refresh token pair and persists the refresh token's correlation row. */
	private AuthResponse issueTokens(User user) {
		String accessToken = jwtTokenProvider.generateAccessToken(user);

		UUID tokenId = UUID.randomUUID();
		String refreshToken = jwtTokenProvider.generateRefreshToken(user, tokenId);
		OffsetDateTime expiresAt = OffsetDateTime.now().plus(jwtTokenProvider.getRefreshTokenTtl());
		refreshTokenRepository.save(new RefreshToken(tokenId, user.getId(), expiresAt));

		return new AuthResponse(accessToken, refreshToken, "Bearer", userMapper.toUserResponse(user));
	}

	private Claims parseRefreshTokenClaims(String refreshToken) {
		Claims claims;
		try {
			claims = jwtTokenProvider.parseToken(refreshToken).getPayload();
		} catch (JwtException | IllegalArgumentException ex) {
			throw new InvalidRefreshTokenException();
		}

		if (!jwtTokenProvider.isRefreshToken(claims)) {
			throw new InvalidRefreshTokenException();
		}

		return claims;
	}

	/** Reads the user id that {@code JwtAuthenticationFilter} placed in the security context. */
	private Long currentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
			throw new UnauthorizedException("Authentication is required");
		}
		return userId;
	}
}
