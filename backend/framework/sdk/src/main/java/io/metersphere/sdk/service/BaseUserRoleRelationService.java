package io.metersphere.sdk.service;

import io.metersphere.sdk.exception.MSException;
import io.metersphere.system.domain.UserRole;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.domain.UserRoleRelationExample;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static io.metersphere.sdk.constants.InternalUserRole.ADMIN;
import static io.metersphere.sdk.controller.handler.result.CommonResultCode.*;

/**
 * @author jianxing
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BaseUserRoleRelationService {

    @Resource
    protected UserRoleRelationMapper userRoleRelationMapper;
    @Resource
    @Lazy
    protected BaseUserRoleService baseUserRoleService;


    protected UserRoleRelation add(UserRoleRelation userRoleRelation) {
        checkExist(userRoleRelation);
        userRoleRelation.setCreateTime(System.currentTimeMillis());
        userRoleRelation.setId(UUID.randomUUID().toString());
        userRoleRelationMapper.insert(userRoleRelation);
        return userRoleRelation;
    }

    /**
     * 校验用户是否已在当前用户组
     */
    public void checkExist(UserRoleRelation userRoleRelation) {
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria()
                .andUserIdEqualTo(userRoleRelation.getUserId())
                .andRoleIdEqualTo(userRoleRelation.getRoleId());

        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(userRoleRelations)) {
            throw new MSException(USER_ROLE_RELATION_EXIST);
        }
    }

    public UserRole getUserRole(String id) {
        UserRoleRelation userRoleRelation = userRoleRelationMapper.selectByPrimaryKey(id);
        return baseUserRoleService.get(userRoleRelation.getRoleId());
    }

    protected void delete(String id) {
        UserRoleRelation userRoleRelation = userRoleRelationMapper.selectByPrimaryKey(id);
        checkAdminPermissionRemove(userRoleRelation.getUserId(), userRoleRelation.getRoleId());
        userRoleRelationMapper.deleteByPrimaryKey(id);
    }

    public void deleteByRoleId(String roleId) {
        List<UserRoleRelation> userRoleRelations = getByRoleId(roleId);
        userRoleRelations.forEach(userRoleRelation ->
                checkAdminPermissionRemove(userRoleRelation.getUserId(), userRoleRelation.getRoleId()));
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria().andRoleIdEqualTo(roleId);
        userRoleRelationMapper.deleteByExample(example);
    }

    public List<UserRoleRelation> getByRoleId(String roleId) {
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria().andRoleIdEqualTo(roleId);
        return userRoleRelationMapper.selectByExample(example);
    }

    /**
     * admin 不能从系统管理员用户组删除
     */
    private static void checkAdminPermissionRemove(String userId, String roleId) {
        if (StringUtils.equals(roleId, ADMIN.getValue()) && StringUtils.equals(userId, ADMIN.getValue())) {
            throw new MSException(USER_ROLE_RELATION_REMOVE_ADMIN_USER_PERMISSION);
        }
    }

    public String getLogDetails(String id) {
        UserRoleRelation userRoleRelation = userRoleRelationMapper.selectByPrimaryKey(id);
        if (userRoleRelation != null) {
            UserRole userRole = baseUserRoleService.get(userRoleRelation.getRoleId());
            return userRole == null ? null : userRole.getName();
        }
        return null;
    }
}
