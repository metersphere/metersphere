package io.metersphere.system.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.metersphere.sdk.mapper.UserExtendMapper;
import io.metersphere.sdk.mapper.UserMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserExtend;
import io.metersphere.sdk.dto.UserDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserExtendMapper userExtendMapper;

    public boolean save(UserDTO entity) {
        userMapper.insert(entity);

        UserExtend userExtend = new UserExtend();
        BeanUtils.copyBean(userExtend, entity);
        userExtendMapper.insert(userExtend);
        return true;
    }

    public UserDTO getById(String id) {
        return userMapper.selectById(id);
    }

    public List<User> list() {
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>(new User());
        return userMapper.selectList(userQueryWrapper);
    }
}
