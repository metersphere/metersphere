package io.metersphere.system.service;

import io.metersphere.sdk.exception.MSException;
import io.metersphere.system.domain.UserRole;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.domain.UserRoleRelationExample;
import io.metersphere.sdk.dto.UserRoleRelationUserDTO;
import io.metersphere.system.mapper.ExtUserRoleRelationMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import java.util.List;
import java.util.UUID;

import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import io.metersphere.system.dto.request.GlobalUserRoleRelationQueryRequest;

import static io.metersphere.sdk.constants.InternalUserRole.ADMIN;
import static io.metersphere.system.controller.result.SystemResultCode.*;

/**
 * @author jianxing
 * @date : 2023-6-12
 */
@Service
public class GlobalUserRoleRelationService {

    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;
    @Resource
    private ExtUserRoleRelationMapper extUserRoleRelationMapper;
    @Resource
    private GlobalUserRoleService globalUserRoleService;

    public List<UserRoleRelationUserDTO> list(GlobalUserRoleRelationQueryRequest request) {
        UserRole userRole = globalUserRoleService.get(request.getRoleId());
        globalUserRoleService.checkSystemUserGroup(userRole);
        globalUserRoleService.checkGlobalUserRole(userRole);
        return extUserRoleRelationMapper.listGlobal(request);
    }

    public UserRoleRelation add(UserRoleRelation userRoleRelation) {
        UserRole userRole = globalUserRoleService.get(userRoleRelation.getRoleId());
        checkExist(userRoleRelation);
        globalUserRoleService.checkSystemUserGroup(userRole);
        globalUserRoleService.checkGlobalUserRole(userRole);

        userRoleRelation.setSourceId(GlobalUserRoleService.SYSTEM_TYPE);
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
            throw new MSException(GLOBAL_USER_ROLE_RELATION_EXIST);
        }
    }

    public void delete(String id) {
        UserRoleRelation userRoleRelation = userRoleRelationMapper.selectByPrimaryKey(id);
        UserRole userRole = globalUserRoleService.get(userRoleRelation.getRoleId());
        checkAdminPermissionRemove(userRoleRelation, userRole);
        globalUserRoleService.checkSystemUserGroup(userRole);
        globalUserRoleService.checkGlobalUserRole(userRole);
        userRoleRelationMapper.deleteByPrimaryKey(id);
    }

    /**
     * admin 不能从系统管理员用户组删除
     */
    private static void checkAdminPermissionRemove(UserRoleRelation userRoleRelation, UserRole userRole) {
        if (StringUtils.equals(userRole.getId(), ADMIN.getValue()) && StringUtils.equals(userRoleRelation.getUserId(), ADMIN.getValue())) {
            throw new MSException(GLOBAL_USER_ROLE_RELATION_REMOVE_ADMIN_USER_PERMISSION);
        }
    }

    public String getLogDetails(String id) {
        UserRoleRelation userRoleRelation = userRoleRelationMapper.selectByPrimaryKey(id);
        if (userRoleRelation != null) {
            UserRole userRole = globalUserRoleService.get(userRoleRelation.getRoleId());
            return userRole == null ? null : userRole.getName();
        }
        return null;
    }
}
