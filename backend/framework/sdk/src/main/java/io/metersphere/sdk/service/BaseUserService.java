package io.metersphere.sdk.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.metersphere.system.domain.User;
import io.metersphere.sdk.mapper.UserMapper;
import org.springframework.stereotype.Service;


@Service
public class BaseUserService extends ServiceImpl<UserMapper, User> {
}
