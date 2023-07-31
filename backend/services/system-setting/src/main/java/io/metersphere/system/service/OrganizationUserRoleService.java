package io.metersphere.system.service;

import io.metersphere.sdk.constants.InternalUserRole;
import io.metersphere.sdk.dto.PermissionDefinitionItem;
import io.metersphere.sdk.dto.request.PermissionSettingUpdateRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.service.BaseUserRoleService;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.*;
import io.metersphere.system.mapper.*;
import io.metersphere.system.request.OrganizationUserRoleMemberEditRequest;
import io.metersphere.system.request.OrganizationUserRoleMemberRequest;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static io.metersphere.system.controller.result.SystemResultCode.NO_ORG_USER_ROLE_PERMISSION;

/**
 * 组织-用户组与权限
 * @author song-cc-rock
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationUserRoleService extends BaseUserRoleService {

    public static final String ORGANIZATION_ROLE_TYPE = "ORGANIZATION";
    public static final String ORGANIZATION_ROLE_SCOPE = "global";

    @Resource
    UserMapper userMapper;
    @Resource
    UserRoleMapper userRoleMapper;
    @Resource
    ExtUserRoleMapper extUserRoleMapper;
    @Resource
    UserRoleRelationMapper userRoleRelationMapper;
    @Resource
    UserRolePermissionMapper userRolePermissionMapper;
    @Resource
    ExtUserRoleRelationMapper extUserRoleRelationMapper;

    public List<UserRole> list(String organizationId) {
        UserRoleExample example = new UserRoleExample();
        example.createCriteria().andTypeEqualTo(ORGANIZATION_ROLE_TYPE)
                .andScopeIdIn(Arrays.asList(organizationId, ORGANIZATION_ROLE_SCOPE));
        return userRoleMapper.selectByExample(example);
    }

    @Override
    public UserRole add(UserRole userRole) {
        userRole.setInternal(false);
        checkNewRoleExist(userRole);
        return super.add(userRole);
    }

    @Override
    public UserRole update(UserRole userRole) {
        UserRole oldRole = get(userRole.getId());
        // 非组织用户组不允许修改, 内置用户组不允许修改
        checkOrgUserRole(oldRole);
        checkInternalUserRole(oldRole);
        checkNewRoleExist(userRole);
        return super.update(userRole);
    }

    public void delete(String roleId, String currentUserId) {
        UserRole oldRole = get(roleId);
        // 非组织用户组不允许删除, 内置用户组不允许删除
        checkOrgUserRole(oldRole);
        checkInternalUserRole(oldRole);
        // 删除用户组
        userRoleMapper.deleteByPrimaryKey(roleId);
        UserRoleRelationExample relationExample = new UserRoleRelationExample();
        relationExample.createCriteria().andRoleIdEqualTo(roleId).andSourceIdEqualTo(oldRole.getScopeId());
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(relationExample);
        List<UserRoleRelation> orgMemberRelations = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(userRoleRelations)) {
            // 如果删除的组织用户组内成员只有当前一个用户组，则给该成员赋予组织成员用户组
            List<String> userIds = userRoleRelations.stream().map(UserRoleRelation::getUserId).toList();
            UserRoleRelationExample userRelationExample = new UserRoleRelationExample();
            userRelationExample.createCriteria().andUserIdIn(userIds).andSourceIdEqualTo(oldRole.getScopeId());
            List<UserRoleRelation> allUserRelations = userRoleRelationMapper.selectByExample(userRelationExample);
            Map<String, List<UserRoleRelation>> userRoleRelationMap = allUserRelations.stream().collect(Collectors.groupingBy(UserRoleRelation::getUserId));
            userRoleRelationMap.forEach((userId, relations) -> {
                if (relations.size() == 1) {
                    UserRoleRelation relation = new UserRoleRelation();
                    relation.setId(UUID.randomUUID().toString());
                    relation.setUserId(userId);
                    relation.setSourceId(oldRole.getScopeId());
                    relation.setRoleId(InternalUserRole.ORG_MEMBER.getValue());
                    relation.setCreateTime(System.currentTimeMillis());
                    relation.setCreateUser(currentUserId);
                    orgMemberRelations.add(relation);
                }
            });
        }
        if (CollectionUtils.isNotEmpty(orgMemberRelations)) {
            extUserRoleRelationMapper.batchInsert(orgMemberRelations);
        }
        userRoleRelationMapper.deleteByExample(relationExample);
        UserRolePermissionExample permissionExample = new UserRolePermissionExample();
        permissionExample.createCriteria().andRoleIdEqualTo(roleId);
        userRolePermissionMapper.deleteByExample(permissionExample);
    }

    public List<User> listMember(OrganizationUserRoleMemberRequest request) {
        return extUserRoleMapper.listOrganizationRoleMember(request);
    }

    public void addMember(OrganizationUserRoleMemberEditRequest request, String createUserId) {
        request.getUserIds().forEach(userId -> {
            checkMemberParam(userId, request.getUserRoleId());
            UserRoleRelation relation = new UserRoleRelation();
            relation.setId(UUID.randomUUID().toString());
            relation.setUserId(userId);
            relation.setRoleId(request.getUserRoleId());
            relation.setSourceId(request.getOrganizationId());
            relation.setCreateTime(System.currentTimeMillis());
            relation.setCreateUser(createUserId);
            userRoleRelationMapper.insert(relation);
        });
    }

    public void removeMember(OrganizationUserRoleMemberEditRequest request) {
        String removeUserId = request.getUserIds().get(0);
        checkMemberParam(removeUserId, request.getUserRoleId());
        // 移除组织-用户组的成员, 若成员只存在该组织下唯一用户组, 则提示不能移除
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria().andUserIdEqualTo(removeUserId)
                .andRoleIdNotEqualTo(request.getUserRoleId())
                .andSourceIdEqualTo(request.getOrganizationId());
        if (userRoleRelationMapper.countByExample(example) == 0) {
            throw new MSException(Translator.get("at_least_one_user_role_require"));
        }
        example.clear();
        example.createCriteria().andUserIdEqualTo(removeUserId)
                .andRoleIdEqualTo(request.getUserRoleId())
                .andSourceIdEqualTo(request.getOrganizationId());
        userRoleRelationMapper.deleteByExample(example);
    }

    public List<PermissionDefinitionItem> getPermissionSetting(String id) {
        UserRole userRole = get(id);
        checkOrgUserRole(userRole);
        return getPermissionSetting(userRole);
    }

    @Override
    public void updatePermissionSetting(PermissionSettingUpdateRequest request) {
        UserRole userRole = get(request.getUserRoleId());
        checkOrgUserRole(userRole);
        checkInternalUserRole(userRole);
        super.updatePermissionSetting(request);
    }

    @Override
    public UserRole get(String id) {
        UserRole userRole = userRoleMapper.selectByPrimaryKey(id);
        if (userRole == null) {
            throw new MSException(Translator.get("user_role_not_exist"));
        }
        return userRole;
    }

    /**
     * 校验是否组织下用户组
     * @param userRole 用户组
     */
    private void checkOrgUserRole(UserRole userRole) {
        if (!ORGANIZATION_ROLE_TYPE.equals(userRole.getType())) {
            throw new MSException(NO_ORG_USER_ROLE_PERMISSION);
        }
    }

    /**
     * 校验同名用户组是否存在
     * @param userRole 用户组
     */
    private void checkNewRoleExist(UserRole userRole) {
        UserRoleExample example = new UserRoleExample();
        UserRoleExample.Criteria criteria = example.createCriteria().andNameEqualTo(userRole.getName())
                .andScopeIdIn(Arrays.asList(userRole.getScopeId(), ORGANIZATION_ROLE_SCOPE))
                .andTypeEqualTo(userRole.getType());
        if (userRole.getId() != null) {
            criteria.andIdNotEqualTo(userRole.getId());
        }
        List<UserRole> userRoles = userRoleMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(userRoles)) {
            throw new MSException(Translator.get("user_role_exist"));
        }
    }

    /**
     * 校验用户与用户组是否存在
     * @param userId 用户ID
     * @param roleId 用户组ID
     */
    private void checkMemberParam(String userId, String roleId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new MSException(Translator.get("user_not_exist"));
        }
        UserRole userRole = userRoleMapper.selectByPrimaryKey(roleId);
        if (userRole == null) {
            throw new MSException(Translator.get("user_role_not_exist"));
        }
    }
}
