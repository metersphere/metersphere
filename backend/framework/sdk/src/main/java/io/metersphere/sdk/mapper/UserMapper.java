package io.metersphere.sdk.mapper;

import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.system.domain.User;

import java.util.List;

public interface UserMapper {
    UserDTO selectById(String id);

    List<User> findAll();
}
