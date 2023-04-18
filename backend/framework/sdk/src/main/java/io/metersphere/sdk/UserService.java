package io.metersphere.sdk;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.metersphere.domain.User;
import io.metersphere.sdk.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {
}
