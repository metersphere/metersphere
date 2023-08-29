package io.metersphere.sdk.service;

import io.metersphere.sdk.constants.UserRoleEnum;
import io.metersphere.sdk.dto.Permission;
import io.metersphere.sdk.dto.PermissionDefinitionItem;
import io.metersphere.sdk.dto.request.PermissionSettingUpdateRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.uid.UUID;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.PermissionCache;
import io.metersphere.sdk.util.ServiceUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserRole;
import io.metersphere.system.domain.UserRoleExample;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.mapper.UserRoleMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import io.metersphere.sdk.uid.UUID;
import java.util.stream.Collectors;

import static io.metersphere.sdk.controller.handler.result.CommonResultCode.INTERNAL_USER_ROLE_PERMISSION;

/**
 * @author jianxing
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BaseUserRoleService {
    @Resource
    private PermissionCache permissionCache;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    protected BaseUserRolePermissionService baseUserRolePermissionService;
    @Resource
    protected BaseUserRoleRelationService baseUserRoleRelationService;

    /**
     * 根据用户组获取对应的权限配置项
     *
     * @param userRole
     * @return
     */
    public List<PermissionDefinitionItem> getPermissionSetting(UserRole userRole) {
        // 获取该用户组拥有的权限
        Set<String> permissionIds = baseUserRolePermissionService.getPermissionIdSetByRoleId(userRole.getId());
        // 获取所有的权限
        List<PermissionDefinitionItem> permissionDefinition = permissionCache.getPermissionDefinition();
        // 深拷贝
        permissionDefinition = JSON.parseArray(JSON.toJSONString(permissionDefinition), PermissionDefinitionItem.class);

        // 过滤该用户组级别的菜单，例如系统级别
        permissionDefinition = permissionDefinition.stream()
                .filter(item -> StringUtils.equals(item.getType(), userRole.getType()))
                .toList();

        // 设置勾选项
        for (PermissionDefinitionItem firstLevel : permissionDefinition) {
            List<PermissionDefinitionItem> children = firstLevel.getChildren();
            boolean allCheck = true;
            firstLevel.setName(Translator.get(firstLevel.getName()));
            for (PermissionDefinitionItem secondLevel : children) {
                List<Permission> permissions = secondLevel.getPermissions();
                secondLevel.setName(Translator.get(secondLevel.getName()));
                if (CollectionUtils.isEmpty(permissions)) {
                    continue;
                }
                boolean secondAllCheck = true;
                for (Permission p : permissions) {
                    if (StringUtils.isNotBlank(p.getName())) {
                        // 有 name 字段翻译 name 字段
                        p.setName(Translator.get(p.getName()));
                    } else {
                        p.setName(translateDefaultPermissionName(p));
                    }
                    if (permissionIds.contains(p.getId())) {
                        p.setEnable(true);
                    } else {
                        // 如果权限有未勾选，则二级菜单设置为未勾选
                        p.setEnable(false);
                        secondAllCheck = false;
                    }
                }
                secondLevel.setEnable(secondAllCheck);
                if (!secondAllCheck) {
                    // 如果二级菜单有未勾选，则一级菜单设置为未勾选
                    allCheck = false;
                }
            }
            firstLevel.setEnable(allCheck);
        }


        return permissionDefinition;
    }

    private String translateDefaultPermissionName(Permission p) {
        if (StringUtils.isNotBlank(p.getName())) {
            p.getName();
        }
        String[] idSplit = p.getId().split(":");
        String permissionKey = idSplit[idSplit.length - 1];
        Map<String, String> translationMap = new HashMap<>() {{
            put("READ", "permission.read");
            put("READ+ADD", "permission.add");
            put("READ+UPDATE", "permission.edit");
            put("READ+DELETE", "permission.delete");
            put("READ+IMPORT", "permission.import");
            put("READ+RECOVER", "permission.recover");
        }};
        return Translator.get(translationMap.get(permissionKey));
    }

    /**
     * 更新单个用户组的配置项
     *
     * @param request
     */
    protected void updatePermissionSetting(PermissionSettingUpdateRequest request) {
        baseUserRolePermissionService.updatePermissionSetting(request);
    }

    protected UserRole add(UserRole userRole) {
        userRole.setId(UUID.randomUUID().toString());
        userRole.setCreateTime(System.currentTimeMillis());
        userRole.setUpdateTime(System.currentTimeMillis());
        userRoleMapper.insert(userRole);
        return userRole;
    }

    protected UserRole update(UserRole userRole) {
        userRole.setCreateUser(null);
        userRole.setCreateTime(null);
        userRole.setType(null);
        userRole.setUpdateTime(System.currentTimeMillis());
        userRoleMapper.updateByPrimaryKeySelective(userRole);
        return userRole;
    }

    public UserRole checkResourceExist(UserRole userRole) {
        return ServiceUtils.checkResourceExist(userRole, "permission.system_user_role.name");
    }

    /**
     * 删除用户组，并且删除用户组与用户的关联关系，用户组与权限的关联关系
     *
     * @param userRole
     */
    public void delete(UserRole userRole, String defaultRoleId, String currentUserId, String orgId) {
        String id = userRole.getId();
        checkInternalUserRole(userRole);

        // 删除用户组的权限设置
        baseUserRolePermissionService.deleteByRoleId(id);

        // 删除用户组
        userRoleMapper.deleteByPrimaryKey(id);

        // 检查是否只有一个用户组，如果是则添加系统成员等默认用户组
        checkOneLimitRole(id, defaultRoleId, currentUserId, orgId);

        // 删除用户组与用户的关联关系
        baseUserRoleRelationService.deleteByRoleId(id);
    }

    /**
     * 校验是否是内置用户组，是内置抛异常
     */
    public void checkInternalUserRole(UserRole userRole) {
        if (BooleanUtils.isTrue(userRole.getInternal())) {
            throw new MSException(INTERNAL_USER_ROLE_PERMISSION);
        }
    }

    public UserRole get(String id) {
        return userRoleMapper.selectByPrimaryKey(id);
    }

    public UserRole getWithCheck(String id) {
        return checkResourceExist(userRoleMapper.selectByPrimaryKey(id));
    }

    public List<UserRole> getList(List<String> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return new ArrayList<>();
        } else {
            UserRoleExample example = new UserRoleExample();
            example.createCriteria().andIdIn(idList);
            return userRoleMapper.selectByExample(example);
        }
    }

    public String getLogDetails(String id) {
        UserRole userRole = userRoleMapper.selectByPrimaryKey(id);
        return userRole == null ? null : userRole.getName();
    }

    /**
     * 删除用户组时校验必须要有一个用户组
     * 没有的话，添加系统成员，组织成员，项目成员用户组
     *
     * @param defaultRoleId 默认用户组id
     * @param currentUserId 当前用户id
     */
    public void checkOneLimitRole(String roleId, String defaultRoleId, String currentUserId, String orgId) {

        // 查询要删除的用户组关联的用户ID
        List<String> userIds = baseUserRoleRelationService.getUserIdByRoleId(roleId);

        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }

        // 查询用户列表与所有用户组的关联关系，并分组（UserRoleRelation 中只有 userId 和 sourceId）
        Map<String, List<UserRoleRelation>> userRoleRelationMap = baseUserRoleRelationService
                .getUserIdAndSourceIdByUserIds(userIds)
                .stream()
                .collect(Collectors.groupingBy(i -> i.getUserId() + i.getSourceId()));

        List<UserRoleRelation> addRelations = new ArrayList<>();
        userRoleRelationMap.forEach((groupId, relations) -> {
            // 如果当前用户组只有一个用户，并且就是要删除的用户组，则添加组织成员等默认用户组
            if (relations.size() == 1 && StringUtils.equals(relations.get(0).getRoleId(), roleId)) {
                UserRoleRelation relation = new UserRoleRelation();
                relation.setId(UUID.randomUUID().toString());
                relation.setUserId(relations.get(0).getUserId());
                relation.setSourceId(relations.get(0).getSourceId());
                relation.setRoleId(defaultRoleId);
                relation.setCreateTime(System.currentTimeMillis());
                relation.setCreateUser(currentUserId);
                relation.setOrganizationId(orgId);
                addRelations.add(relation);
            }
        });

        baseUserRoleRelationService.batchInsert(addRelations);
    }

    /**
     * 校验同名用户组是否存在
     * @param userRole 用户组
     */
    public void checkNewRoleExist(UserRole userRole) {
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
    public void checkMemberParam(String userId, String roleId) {
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
