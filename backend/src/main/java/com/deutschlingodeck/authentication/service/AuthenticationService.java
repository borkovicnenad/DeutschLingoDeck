package com.deutschlingodeck.authentication.service;

import com.deutschlingodeck.authentication.dto.AuthResponse;
import com.deutschlingodeck.authentication.dto.LoginRequest;
import com.deutschlingodeck.authentication.dto.RefreshTokenRequest;
import com.deutschlingodeck.authentication.dto.RegisterRequest;
import com.deutschlingodeck.authentication.dto.UserResponse;

public interface AuthenticationService {

	AuthResponse register(RegisterRequest request);

	AuthResponse login(LoginRequest request);

	AuthResponse refresh(RefreshTokenRequest request);

	void logout();

	UserResponse getCurrentUser();
}
