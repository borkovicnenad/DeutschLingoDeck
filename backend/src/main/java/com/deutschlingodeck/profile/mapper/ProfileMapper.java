package com.deutschlingodeck.profile.mapper;

import com.deutschlingodeck.authentication.entity.User;
import com.deutschlingodeck.common.mapper.MapStructConfig;
import com.deutschlingodeck.profile.dto.ProfileResponse;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface ProfileMapper {

	ProfileResponse toProfileResponse(User user);
}
