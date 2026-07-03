package com.deutschlingodeck.authentication.controller;

import com.deutschlingodeck.authentication.dto.AuthResponse;
import com.deutschlingodeck.authentication.dto.LoginRequest;
import com.deutschlingodeck.authentication.dto.RefreshTokenRequest;
import com.deutschlingodeck.authentication.dto.RegisterRequest;
import com.deutschlingodeck.authentication.dto.UserResponse;
import com.deutschlingodeck.authentication.service.AuthenticationService;
import com.deutschlingodeck.common.constants.ApiConstants;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.API_BASE_PATH + "/auth")
public class AuthenticationController {

	private final AuthenticationService authenticationService;

	public AuthenticationController(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authenticationService.register(request));
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authenticationService.login(request));
	}

	@PostMapping("/refresh")
	public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
		return ResponseEntity.ok(authenticationService.refresh(request));
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout() {
		authenticationService.logout();
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/me")
	public ResponseEntity<UserResponse> getCurrentUser() {
		return ResponseEntity.ok(authenticationService.getCurrentUser());
	}
}
