package io.metersphere.sdk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.system.domain.User;

public interface UserMapper extends BaseMapper<User> {
    UserDTO selectById(String id);
}
