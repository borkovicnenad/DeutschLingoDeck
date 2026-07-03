package com.deutschlingodeck.profile.service;

import com.deutschlingodeck.authentication.repository.UserRepository;
import com.deutschlingodeck.profile.dto.ChangePasswordRequest;
import com.deutschlingodeck.profile.dto.ProfileResponse;
import com.deutschlingodeck.profile.dto.UpdateProfileRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ProfileServiceImpl implements ProfileService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public ProfileServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public ProfileResponse getProfile() {
		// TODO: resolve the current user from the security context and map to ProfileResponse.
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public ProfileResponse updateProfile(UpdateProfileRequest request) {
		// TODO: update the current user's displayName and persist.
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public void changePassword(ChangePasswordRequest request) {
		// TODO: verify currentPassword with passwordEncoder, then hash and persist newPassword.
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
