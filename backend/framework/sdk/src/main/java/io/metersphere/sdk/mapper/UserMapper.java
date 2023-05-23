package io.metersphere.sdk.mapper;

import io.metersphere.sdk.dto.UserDTO;

public interface UserMapper {
    UserDTO selectById(String id);
}
