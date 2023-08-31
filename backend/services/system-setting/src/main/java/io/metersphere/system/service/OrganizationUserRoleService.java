package io.metersphere.system.service;

import io.metersphere.sdk.constants.InternalUserRole;
import io.metersphere.sdk.constants.UserRoleEnum;
import io.metersphere.sdk.constants.UserRoleType;
import io.metersphere.sdk.dto.PermissionDefinitionItem;
import io.metersphere.sdk.dto.request.PermissionSettingUpdateRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.mapper.BaseUserMapper;
import io.metersphere.sdk.service.BaseUserRoleService;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.UserExtend;
import io.metersphere.system.mapper.ExtUserRoleMapper;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.mapper.UserRoleMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.request.OrganizationUserRoleMemberEditRequest;
import io.metersphere.system.request.OrganizationUserRoleMemberRequest;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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

    @Resource
    UserMapper userMapper;
    @Resource
    BaseUserMapper baseUserMapper;
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
        return userRoleMapper.selectByExample(example);
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
        // 非组织用户组不允许修改, 内置用户组不允许修改
        checkOrgUserRole(oldRole);
        checkInternalUserRole(oldRole);
        userRole.setType(UserRoleType.ORGANIZATION.name());
        checkNewRoleExist(userRole);
        return super.update(userRole);
    }

    public void delete(String roleId, String currentUserId) {
        UserRole userRole = get(roleId);
        // 非组织用户组不允许删除, 内置用户组不允许删除
        checkOrgUserRole(userRole);
        super.delete(userRole, InternalUserRole.ORG_MEMBER.getValue(), currentUserId);
    }

    public List<UserExtend> getMember(String organizationId, String roleId) {
        List<UserExtend> userExtends = new ArrayList<>();
        // 查询所有用户
        List<User> users = baseUserMapper.findAll();
        Map<String, User> userMap = users.stream().collect(Collectors.toMap(User::getId, user -> user));
        // 查询组织下所有用户关系
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria().andSourceIdEqualTo(organizationId);
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(userRoleRelations)) {
            Map<String, List<String>> userRoleMap = userRoleRelations.stream().collect(Collectors.groupingBy(UserRoleRelation::getUserId,
                    Collectors.mapping(UserRoleRelation::getRoleId, Collectors.toList())));
            userRoleMap.forEach((k, v) -> {
                UserExtend userExtend = new UserExtend();
                User user = userMap.get(k);
                if (user != null) {
                    BeanUtils.copyBean(userExtend, user);
                    v.forEach(roleItem -> {
                        if (StringUtils.equals(roleItem, roleId)) {
                            userExtend.setCheckRoleFlag(true);
                        }
                    });
                    userExtends.add(userExtend);
                }
            });
        }
        return userExtends;
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
            relation.setOrganizationId(request.getOrganizationId());
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
        if (!UserRoleType.ORGANIZATION.name().equals(userRole.getType())) {
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
                .andScopeIdIn(Arrays.asList(userRole.getScopeId(), UserRoleEnum.GLOBAL.toString()))
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
