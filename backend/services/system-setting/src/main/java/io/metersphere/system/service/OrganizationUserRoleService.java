package io.metersphere.system.service;

import com.alibaba.excel.util.StringUtils;
import io.metersphere.sdk.constants.InternalUserRole;
import io.metersphere.sdk.constants.UserRoleEnum;
import io.metersphere.sdk.constants.UserRoleType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.permission.PermissionDefinitionItem;
import io.metersphere.system.dto.request.OrganizationUserRoleMemberEditRequest;
import io.metersphere.system.dto.request.OrganizationUserRoleMemberRequest;
import io.metersphere.system.dto.sdk.request.PermissionSettingUpdateRequest;
import io.metersphere.system.mapper.ExtUserRoleMapper;
import io.metersphere.system.mapper.UserRoleMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static io.metersphere.system.controller.result.SystemResultCode.NO_ORG_USER_ROLE_PERMISSION;

/**
 * 组织-用户组与权限
 *
 * @author song-cc-rock
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationUserRoleService extends BaseUserRoleService {

    @Resource
    UserRoleMapper userRoleMapper;
    @Resource
    ExtUserRoleMapper extUserRoleMapper;
    @Resource
    UserRoleRelationMapper userRoleRelationMapper;

    public List<UserRole> list(String organizationId) {
        UserRoleExample example = new UserRoleExample();
        example.createCriteria().andTypeEqualTo(UserRoleType.ORGANIZATION.name())
                .andScopeIdIn(Arrays.asList(organizationId, UserRoleEnum.GLOBAL.toString()));
        example.setOrderByClause("create_time asc");
        List<UserRole> userRoles = userRoleMapper.selectByExample(example);
        userRoles.sort(Comparator.comparing(UserRole::getInternal).thenComparing(UserRole::getScopeId)
                .thenComparing(Comparator.comparing(UserRole::getCreateTime).thenComparing(UserRole::getId).reversed()).reversed());
        return userRoles;
    }

    @Override
    public UserRole add(UserRole userRole) {
        userRole.setInternal(false);
        userRole.setType(UserRoleType.ORGANIZATION.name());
        checkNewRoleExist(userRole);
        return super.add(userRole);
    }

    @Override
    public UserRole update(UserRole userRole) {
        UserRole oldRole = get(userRole.getId());
        // 非组织用户组不允许修改, 全局用户组不允许修改
        checkOrgUserRole(oldRole);
        checkGlobalUserRole(oldRole);
        userRole.setType(UserRoleType.ORGANIZATION.name());
        checkNewRoleExist(userRole);
        return super.update(userRole);
    }

    public void delete(String roleId, String currentUserId) {
        UserRole userRole = get(roleId);
        // 非组织用户组不允许删除, 内置全局用户组不允许删除
        checkOrgUserRole(userRole);
        checkGlobalUserRole(userRole);
        super.delete(userRole, InternalUserRole.ORG_MEMBER.getValue(), currentUserId, userRole.getScopeId());
    }

    public List<User> listMember(OrganizationUserRoleMemberRequest request) {
        return extUserRoleMapper.listOrganizationRoleMember(request);
    }

    public void addMember(OrganizationUserRoleMemberEditRequest request, String createUserId) {
        request.getUserIds().forEach(userId -> {
            checkMemberParam(userId, request.getUserRoleId());
            UserRoleRelation relation = new UserRoleRelation();
            relation.setId(IDGenerator.nextStr());
            relation.setUserId(userId);
            relation.setRoleId(request.getUserRoleId());
            relation.setSourceId(request.getOrganizationId());
            relation.setCreateTime(System.currentTimeMillis());
            relation.setCreateUser(createUserId);
            relation.setOrganizationId(request.getOrganizationId());
            userRoleRelationMapper.insert(relation);
        });
    }

    public void removeMember(OrganizationUserRoleMemberEditRequest request) {
        String removeUserId = request.getUserIds().getFirst();
        checkMemberParam(removeUserId, request.getUserRoleId());
        //检查移除的是不是管理员
        if (StringUtils.equals(request.getUserRoleId(),InternalUserRole.ORG_ADMIN.getValue())) {
            UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
            userRoleRelationExample.createCriteria().andUserIdNotEqualTo(removeUserId)
                    .andSourceIdEqualTo(request.getOrganizationId())
                    .andRoleIdEqualTo(InternalUserRole.ORG_ADMIN.getValue());
            if (userRoleRelationMapper.countByExample(userRoleRelationExample) == 0) {
                throw new MSException(Translator.get("keep_at_least_one_administrator"));
            }
        }
        // 移除组织-用户组的成员, 若成员只存在该组织下唯一用户组, 则提示不能移除
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria().andUserIdEqualTo(removeUserId)
                .andRoleIdNotEqualTo(request.getUserRoleId())
                .andSourceIdEqualTo(request.getOrganizationId());
        if (userRoleRelationMapper.countByExample(example) == 0) {
            throw new MSException(Translator.get("org_at_least_one_user_role_require"));
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
     * 校验是否组织下用户组
     *
     * @param userRole 用户组
     */
    private void checkOrgUserRole(UserRole userRole) {
        if (!UserRoleType.ORGANIZATION.name().equals(userRole.getType())) {
            throw new MSException(NO_ORG_USER_ROLE_PERMISSION);
        }
    }
}
