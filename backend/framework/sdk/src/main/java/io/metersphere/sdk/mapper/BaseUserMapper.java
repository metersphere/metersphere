package io.metersphere.sdk.mapper;

import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.system.domain.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseUserMapper {
    UserDTO selectById(String id);

    List<User> findAll();

    void batchSave(@Param("users") List<User> users);

    boolean isSuperUser(String userId);
}
