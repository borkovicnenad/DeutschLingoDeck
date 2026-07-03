package com.deutschlingodeck.profile.controller;

import com.deutschlingodeck.common.constants.ApiConstants;
import com.deutschlingodeck.profile.dto.ChangePasswordRequest;
import com.deutschlingodeck.profile.dto.ProfileResponse;
import com.deutschlingodeck.profile.dto.UpdateProfileRequest;
import com.deutschlingodeck.profile.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.API_BASE_PATH + "/profile")
public class ProfileController {

	private final ProfileService profileService;

	public ProfileController(ProfileService profileService) {
		this.profileService = profileService;
	}

	@GetMapping
	public ResponseEntity<ProfileResponse> getProfile() {
		return ResponseEntity.ok(profileService.getProfile());
	}

	@PutMapping
	public ResponseEntity<ProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
		return ResponseEntity.ok(profileService.updateProfile(request));
	}

	@PutMapping("/password")
	public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
		profileService.changePassword(request);
		return ResponseEntity.noContent().build();
	}
}
