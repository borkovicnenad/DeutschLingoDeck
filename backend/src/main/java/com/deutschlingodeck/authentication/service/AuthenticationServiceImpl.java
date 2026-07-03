package com.deutschlingodeck.authentication.service;

import com.deutschlingodeck.authentication.dto.AuthResponse;
import com.deutschlingodeck.authentication.dto.LoginRequest;
import com.deutschlingodeck.authentication.dto.RefreshTokenRequest;
import com.deutschlingodeck.authentication.dto.RegisterRequest;
import com.deutschlingodeck.authentication.dto.UserResponse;
import com.deutschlingodeck.authentication.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public AuthenticationServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public AuthResponse register(RegisterRequest request) {
		// TODO: validate email uniqueness, hash the password with passwordEncoder,
		// persist the User via userRepository and issue access/refresh JWT tokens.
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public AuthResponse login(LoginRequest request) {
		// TODO: load the user by email, verify the password with passwordEncoder
		// and issue access/refresh JWT tokens.
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public AuthResponse refresh(RefreshTokenRequest request) {
		// TODO: validate the refresh token and issue a new access/refresh token pair.
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public void logout() {
		// TODO: invalidate the refresh token for the currently authenticated user
		// once the security context carries the authenticated principal.
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public UserResponse getCurrentUser() {
		// TODO: resolve the current user from the security context.
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
