package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.RoleMapper;
import io.metersphere.base.mapper.UserMapper;
import io.metersphere.base.mapper.UserRoleMapper;
import io.metersphere.commons.MSException;
import io.metersphere.dto.UserDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private RoleMapper roleMapper;
    @Resource
    private UserRoleMapper userRoleMapper;

    public UserDTO insert(User user) {
        checkUserParam(user);
        createUser(user);
        return getUserDTO(user.getId());
    }

    private void checkUserParam(User user) {
        if (StringUtils.isBlank(user.getName())) {
            MSException.throwException("user_name_empty");
        }

        if (StringUtils.isBlank(user.getEmail())) {
            MSException.throwException("user_email_empty");
        }
        // password
    }

    private void createUser(User userRequest) {
        User user = new User();
        BeanUtils.copyProperties(userRequest, user);
        user.setCreateTime(System.currentTimeMillis());
        user.setUpdateTime(System.currentTimeMillis());
        // 默认1:启用状态
        user.setStatus("1");

        UserExample userExample = new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();
        criteria.andEmailEqualTo(user.getEmail());
        List<User> userList = userMapper.selectByExample(userExample);
        if (!CollectionUtils.isEmpty(userList)) {
            MSException.throwException("user_email_is_exist");
        }
        userMapper.insertSelective(user);
    }

    public UserDTO getUserDTO(String userId) {

        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return null;
        }
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        //
        UserRoleExample userRoleExample = new UserRoleExample();
        userRoleExample.createCriteria().andUserIdEqualTo(userId);
        List<UserRole> userRoleList = userRoleMapper.selectByExample(userRoleExample);

        if (CollectionUtils.isEmpty(userRoleList)) {
            return userDTO;
        }

        List<String> roleIds = userRoleList.stream().map(UserRole::getRoleId).collect(Collectors.toList());

        RoleExample roleExample = new RoleExample();
        roleExample.createCriteria().andIdIn(roleIds);

        List<Role> roleList = roleMapper.selectByExample(roleExample);
        userDTO.setRoles(roleList);

        return userDTO;
    }

    public List<User> getUserList() {
        return userMapper.selectByExample(null);
    }

    public void deleteUser(String userId) {
        userMapper.deleteByPrimaryKey(userId);
    }

    public void updateUser(User user) {
        user.setUpdateTime(System.currentTimeMillis());
        userMapper.updateByPrimaryKeySelective(user);
    }
}
