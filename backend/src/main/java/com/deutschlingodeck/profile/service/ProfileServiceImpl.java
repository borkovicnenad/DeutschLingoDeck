package com.deutschlingodeck.profile.service;

import com.deutschlingodeck.authentication.entity.User;
import com.deutschlingodeck.authentication.repository.UserRepository;
import com.deutschlingodeck.common.exception.UnauthorizedException;
import com.deutschlingodeck.common.exception.ValidationException;
import com.deutschlingodeck.common.security.CurrentUserProvider;
import com.deutschlingodeck.profile.dto.ChangePasswordRequest;
import com.deutschlingodeck.profile.dto.ProfileResponse;
import com.deutschlingodeck.profile.dto.UpdateProfileRequest;
import com.deutschlingodeck.profile.mapper.ProfileMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class ProfileServiceImpl implements ProfileService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final ProfileMapper profileMapper;
	private final CurrentUserProvider currentUserProvider;

	public ProfileServiceImpl(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			ProfileMapper profileMapper,
			CurrentUserProvider currentUserProvider) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.profileMapper = profileMapper;
		this.currentUserProvider = currentUserProvider;
	}

	@Override
	public ProfileResponse getProfile() {
		return profileMapper.toProfileResponse(loadCurrentUser());
	}

	@Override
	@Transactional
	public ProfileResponse updateProfile(UpdateProfileRequest request) {
		User user = loadCurrentUser();
		user.setDisplayName(request.displayName());
		user.setUpdatedAt(OffsetDateTime.now());
		return profileMapper.toProfileResponse(user);
	}

	@Override
	@Transactional
	public void changePassword(ChangePasswordRequest request) {
		User user = loadCurrentUser();
		if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
			throw new ValidationException("The current password is incorrect");
		}
		user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
		user.setUpdatedAt(OffsetDateTime.now());
	}

	private User loadCurrentUser() {
		return userRepository.findById(currentUserProvider.getUserId())
				.orElseThrow(() -> new UnauthorizedException("Authentication is required"));
	}
}
