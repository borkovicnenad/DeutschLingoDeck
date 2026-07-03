package com.deutschlingodeck.profile.service;

import com.deutschlingodeck.profile.dto.ChangePasswordRequest;
import com.deutschlingodeck.profile.dto.ProfileResponse;
import com.deutschlingodeck.profile.dto.UpdateProfileRequest;

public interface ProfileService {

	ProfileResponse getProfile();

	ProfileResponse updateProfile(UpdateProfileRequest request);

	void changePassword(ChangePasswordRequest request);
}
