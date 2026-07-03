package com.deutschlingodeck.authentication.mapper;

import com.deutschlingodeck.authentication.dto.UserResponse;
import com.deutschlingodeck.authentication.entity.User;
import com.deutschlingodeck.common.mapper.MapStructConfig;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface UserMapper {

	UserResponse toUserResponse(User user);
}
