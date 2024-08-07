package io.metersphere.system.service;

import io.metersphere.sdk.exception.MSException;
import io.metersphere.system.domain.UserRole;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.domain.UserRoleRelationExample;
import io.metersphere.system.dto.user.UserExcludeOptionDTO;
import io.metersphere.system.mapper.BaseUserRoleRelationMapper;
import io.metersphere.system.mapper.UserRoleMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.metersphere.sdk.constants.InternalUserRole.ADMIN;
import static io.metersphere.system.controller.handler.result.CommonResultCode.USER_ROLE_RELATION_EXIST;
import static io.metersphere.system.controller.handler.result.CommonResultCode.USER_ROLE_RELATION_REMOVE_ADMIN_USER_PERMISSION;

/**
 * @author jianxing
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BaseUserRoleRelationService {

    @Resource
    protected UserRoleRelationMapper userRoleRelationMapper;
    @Resource
    protected BaseUserRoleRelationMapper baseUserRoleRelationMapper;
    @Resource
    protected UserRoleMapper userRoleMapper;
    @Resource
    private UserLoginService userLoginService;

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
        return userRoleRelation == null ? null : userRoleMapper.selectByPrimaryKey(userRoleRelation.getRoleId());
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
            UserRole userRole = userRoleMapper.selectByPrimaryKey(userRoleRelation.getRoleId());
            return userRole == null ? null : userRole.getName();
        }
        return null;
    }

    public List<String> getUserIdByRoleId(String roleId) {
       return baseUserRoleRelationMapper.getUserIdByRoleId(roleId);
    }

    public List<UserRoleRelation> getUserIdAndSourceIdByUserIds(List<String> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return new ArrayList<>(0);
        }
       return baseUserRoleRelationMapper.getUserIdAndSourceIdByUserIds(userIds);
    }

    public void batchInsert(List<UserRoleRelation> addRelations) {
        if (CollectionUtils.isEmpty(addRelations)) {
            return;
        }
        userRoleRelationMapper.batchInsert(addRelations);
    }

    /**
     * 获取关联用户的下拉框选项
     * 已经关联过的用户，exclude 标记为 true
     * @param roleId
     * @return
     */
    public List<UserExcludeOptionDTO> getExcludeSelectOptionWithLimit(String roleId, String keyword) {
        // 查询所有用户选项
        List<UserExcludeOptionDTO> selectOptions = userLoginService.getExcludeSelectOptionWithLimit(keyword);
        // 查询已经关联的用户ID
        Set<String> excludeUserIds = baseUserRoleRelationMapper.getUserIdByRoleId(roleId)
                .stream()
                .collect(Collectors.toSet());
        // 标记已经关联的用户
        selectOptions.forEach((excludeOption) -> {
            if (excludeUserIds.contains(excludeOption.getId())) {
                excludeOption.setExclude(true);
            }
        });
        return selectOptions;
    }
}
