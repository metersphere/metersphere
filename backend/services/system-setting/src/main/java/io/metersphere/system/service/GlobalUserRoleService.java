package io.metersphere.system.service;

import io.metersphere.system.domain.UserRole;
import io.metersphere.system.domain.UserRoleExample;
import io.metersphere.system.mapper.UserRoleMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统设置的接口增删改查都是针对全局用户组
 * @author jianxing
 * @date : 2023-6-8
 */
@Service
public class GlobalUserRoleService {

    public static String GLOBAL_SCOPE = "GLOBAL";

    @Resource
    private UserRoleMapper userRoleMapper;

    public List<UserRole> list() {
        UserRoleExample example = new UserRoleExample();
        example.createCriteria().andScopeIdEqualTo(GLOBAL_SCOPE);
        return userRoleMapper.selectByExample(example);
    }

    public UserRole get(String id) {
        // todo 只能获取全局
        return userRoleMapper.selectByPrimaryKey(id);
    }

    public UserRole add(UserRole userRole) {
        // todo 只能添加自定义全局
        userRoleMapper.insert(userRole);
        return userRole;
    }

    public UserRole update(UserRole userRole) {
        // todo 只能修改自定义全局
        userRoleMapper.updateByPrimaryKeySelective(userRole);
        return userRole;
    }

    public String delete(String id) {
        // todo 只能删除自定义全局
        userRoleMapper.deleteByPrimaryKey(id);
        return id;
    }
}
