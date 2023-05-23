package io.metersphere.system.service;

import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.sdk.mapper.UserMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserExtend;
import jakarta.annotation.Resource;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private JdbcAggregateTemplate jdbcAggregateTemplate;
    @Resource
    private JdbcTemplate jdbcTemplate;

    public boolean save(UserDTO entity) {
        User user = new User();
        BeanUtils.copyBean(user, entity);
        jdbcAggregateTemplate.insert(user);

        UserExtend userExtend = new UserExtend();
        BeanUtils.copyBean(userExtend, entity);
        jdbcAggregateTemplate.insert(userExtend);
        return true;
    }

    public UserDTO getById(String id) {
        return userMapper.selectById(id);
    }

    public List<User> list() {
        return userMapper.findAll();
    }
}
