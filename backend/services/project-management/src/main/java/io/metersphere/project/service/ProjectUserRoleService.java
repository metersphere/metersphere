package io.metersphere.project.service;

import io.metersphere.project.dto.ProjectUserRoleDTO;
import io.metersphere.project.mapper.ExtProjectUserRoleMapper;
import io.metersphere.project.request.ProjectUserRoleMemberEditRequest;
import io.metersphere.project.request.ProjectUserRoleMemberRequest;
import io.metersphere.project.request.ProjectUserRoleRequest;
import io.metersphere.sdk.constants.InternalUserRole;
import io.metersphere.sdk.constants.UserRoleType;
import io.metersphere.sdk.dto.PermissionDefinitionItem;
import io.metersphere.sdk.dto.request.PermissionSettingUpdateRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserRole;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.domain.UserRoleRelationExample;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.mapper.UserRoleMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.service.BaseUserRoleService;
import io.metersphere.system.uid.UUID;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.metersphere.system.controller.result.SystemResultCode.NO_PROJECT_USER_ROLE_PERMISSION;

/**
 * 项目-用户组与权限
 *
 * @author song-cc-rock
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectUserRoleService extends BaseUserRoleService {

    @Resource
    UserMapper userMapper;
    @Resource
    UserRoleMapper userRoleMapper;
    @Resource
    UserRoleRelationMapper userRoleRelationMapper;
    @Resource
    ExtProjectUserRoleMapper extProjectUserRoleMapper;

    public List<ProjectUserRoleDTO> list(ProjectUserRoleRequest request) {
        List<ProjectUserRoleDTO> roles = extProjectUserRoleMapper.list(request);
        if (CollectionUtils.isEmpty(roles)) {
            return new ArrayList<>();
        }
        List<String> roleIds = roles.stream().map(ProjectUserRoleDTO::getId).toList();
        List<UserRoleRelation> relations = extProjectUserRoleMapper.getRelationByRoleIds(request.getProjectId(), roleIds);
        if (CollectionUtils.isNotEmpty(relations)) {
            Map<String, Long> countMap = relations.stream().collect(Collectors.groupingBy(UserRoleRelation::getRoleId, Collectors.counting()));
            roles.forEach(role -> {
                if (countMap.containsKey(role.getId())) {
                    role.setMemberCount(countMap.get(role.getId()).intValue());
                } else {
                    role.setMemberCount(0);
                }
            });
        } else {
            roles.forEach(role -> {
                role.setMemberCount(0);
            });
        }
        return roles;
    }

    @Override
    public UserRole add(UserRole userRole) {
        userRole.setInternal(false);
        userRole.setType(UserRoleType.PROJECT.name());
        checkNewRoleExist(userRole);
        return super.add(userRole);
    }

    @Override
    public UserRole update(UserRole userRole) {
        UserRole oldRole = get(userRole.getId());
        // 非项目用户组, 全局用户组不允许修改
        checkProjectUserRole(oldRole);
        checkGlobalUserRole(oldRole);
        userRole.setType(UserRoleType.PROJECT.name());
        checkNewRoleExist(userRole);
        return super.update(userRole);
    }

    public void delete(String roleId, String currentUserId) {
        UserRole userRole = get(roleId);
        // 非项目用户组不允许删除, 内置用户组不允许删除
        checkProjectUserRole(userRole);
        super.delete(userRole, InternalUserRole.PROJECT_MEMBER.getValue(), currentUserId, userRole.getScopeId());
    }

    public List<User> listMember(ProjectUserRoleMemberRequest request) {
        return extProjectUserRoleMapper.listProjectRoleMember(request);
    }

    public void addMember(ProjectUserRoleMemberEditRequest request, String createUserId) {
        request.getUserIds().forEach(userId -> {
            checkMemberParam(userId, request.getUserRoleId());
            UserRoleRelation relation = new UserRoleRelation();
            relation.setId(UUID.randomUUID().toString());
            relation.setUserId(userId);
            relation.setRoleId(request.getUserRoleId());
            relation.setSourceId(request.getProjectId());
            relation.setCreateTime(System.currentTimeMillis());
            relation.setCreateUser(createUserId);
            relation.setOrganizationId(request.getProjectId());
            userRoleRelationMapper.insert(relation);
        });
    }

    public void removeMember(ProjectUserRoleMemberEditRequest request) {
        String removeUserId = request.getUserIds().get(0);
        checkMemberParam(removeUserId, request.getUserRoleId());
        // 移除项目-用户组的成员, 若成员只存在该项目下唯一用户组, 则提示不能移除
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria().andUserIdEqualTo(removeUserId)
                .andRoleIdNotEqualTo(request.getUserRoleId())
                .andSourceIdEqualTo(request.getProjectId());
        if (userRoleRelationMapper.countByExample(example) == 0) {
            throw new MSException(Translator.get("at_least_one_user_role_require"));
        }
        example.clear();
        example.createCriteria().andUserIdEqualTo(removeUserId)
                .andRoleIdEqualTo(request.getUserRoleId())
                .andSourceIdEqualTo(request.getProjectId());
        userRoleRelationMapper.deleteByExample(example);
    }

    public List<PermissionDefinitionItem> getPermissionSetting(String id) {
        UserRole userRole = get(id);
        checkProjectUserRole(userRole);
        return getPermissionSetting(userRole);
    }

    @Override
    public void updatePermissionSetting(PermissionSettingUpdateRequest request) {
        UserRole userRole = get(request.getUserRoleId());
        checkProjectUserRole(userRole);
        checkGlobalUserRole(userRole);
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
     * 校验是否项目下用户组
     *
     * @param userRole 用户组
     */
    private void checkProjectUserRole(UserRole userRole) {
        if (!UserRoleType.PROJECT.name().equals(userRole.getType())) {
            throw new MSException(NO_PROJECT_USER_ROLE_PERMISSION);
        }
    }
}
