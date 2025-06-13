package io.metersphere.system.service;


import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.InternalUserRole;
import io.metersphere.sdk.constants.UserRoleType;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.UserRole;
import io.metersphere.system.domain.UserRolePermission;
import io.metersphere.system.dto.user.UserDTO;
import io.metersphere.system.dto.user.UserRoleResourceDTO;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class PermissionCheckService {
    @Resource
    private UserLoginService userLoginService;

    @Resource
    private ProjectMapper projectMapper;

    public static final String API_TEST_MODULE = "apiTest";
    public static final String TEST_PLAN_MODULE = "testPlan";
    public static final String FUNCTIONAL_CASE_MODULE = "caseManagement";
    public static final String BUG_MODULE = "bugManagement";

    /**
     * 检查用户是否有系统或某个组织或者某个项目的某个权限
     *
     * @param userId 用户ID
     * @param sourceId 系统或组织或项目ID
     * @param permission 权限ID
     * @param permissionType 权限类型
     * @return 是否有权限
     */
    public boolean userHasSourcePermission(String userId, String sourceId, String permission, String permissionType) {
        UserDTO user = getUserDTO(userId);
        if (user == null) return false;
        // 判断是否是超级管理员
        if (checkAdmin(user)) return true;
        return checkHasPermission(sourceId, permission, user, permissionType);
    }

    /**
     * 检查用户是否有者某个项目的模块权限
     * @param projectId 项目ID
     * @param module 模块名称
     * @param userId 用户ID
     * @param permission 权限ID
     * @return 是否有权限
     */
    public Boolean checkModule(String projectId, String module, String userId, String permission) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project == null) {
            return false;
        }
        boolean hasPermission = userHasSourcePermission(userId, projectId, permission, UserRoleType.PROJECT.name());
        if (! hasPermission) {
            return false;
        } else {
            List<String> moduleIds = JSON.parseArray(project.getModuleSetting(), String.class);
            return moduleIds.contains(module);
        }
    }

    /**
     * 检查用户是否有系统或某个组织或者某个项目的某个权限
     * @param sourceId 组织或项目ID
     * @param permission 权限ID
     * @param user 用户信息
     * @param permissionType 权限类型
     * @return 是否有权限
     */
    private static boolean checkHasPermission(String sourceId, String permission, UserDTO user, String permissionType) {
        Map<String, List<UserRolePermission>> userRolePermissions = new HashMap<>();
        Map<String, UserRole> role = new HashMap<>();
        getUserAllPermissions(user, userRolePermissions, role);
        Set<String> currentProjectPermissions = user.getUserRoleRelations().stream()
                .filter(ug -> role.get(ug.getId()) != null && StringUtils.equalsIgnoreCase(role.get(ug.getId()).getType(), permissionType))
                .filter(ug -> StringUtils.equalsIgnoreCase(ug.getSourceId(), sourceId))
                .flatMap(ug -> userRolePermissions.get(ug.getId()).stream())
                .map(UserRolePermission::getPermissionId)
                .collect(Collectors.toSet());
        return currentProjectPermissions.contains(permission);
    }

    public UserDTO getUserDTO(String userId) {
        return userLoginService.getUserDTO(userId);
    }

    public boolean checkAdmin(UserDTO user) {
        long count = user.getUserRoles()
                .stream()
                .filter(g -> StringUtils.equalsIgnoreCase(g.getId(), InternalUserRole.ADMIN.getValue()))
                .count();
        return count > 0;
    }

    private static void getUserAllPermissions(UserDTO user, Map<String, List<UserRolePermission>> userRolePermissions, Map<String, UserRole> role) {
        user.getUserRoleRelations().forEach(ug -> user.getUserRolePermissions().forEach(gp -> {
            if (StringUtils.equalsIgnoreCase(gp.getUserRole().getId(), ug.getRoleId())) {
                userRolePermissions.put(ug.getId(), gp.getUserRolePermissions());
                role.put(ug.getId(), gp.getUserRole());
            }
        }));
    }

    /**
     * 获取用户某些权限所占据的项目集合
     *
     * @param userId 用户ID
     * @param projectIds 项目ids
     * @param permissions 需要判断的权限集合
     * @return 有该类型权限的项目ids的map
     */
    public Map<String, Set<String>> getHasUserPermissionProjectIds(String userId, Set<String>projectIds, Set<String> permissions) {
        UserDTO user = getUserDTO(userId);
        if (user == null) return new HashMap<>();
        // 注意超级管理员包含所有权限，这里不予返回，请在方法外自行判断
        Map<String, Set<String>> permissionProjectIdMap = new LinkedHashMap<>();
        Map<String, List<UserRolePermission>> projectPermissionMap = new LinkedHashMap<>();

        Map<String, List<UserRolePermission>>rolePermissionMap = user.getUserRolePermissions().stream().filter(t->StringUtils.equalsIgnoreCase(t.getUserRole().getType(), UserRoleType.PROJECT.name())).collect(Collectors.toMap(f->f.getUserRole().getId(), UserRoleResourceDTO::getUserRolePermissions));

        user.getUserRoleRelations().forEach(ug -> {
            List<UserRolePermission> userRolePermissions = rolePermissionMap.get(ug.getRoleId());
            if (CollectionUtils.isNotEmpty(userRolePermissions) && projectIds.contains(ug.getSourceId())) {
                projectPermissionMap.put(ug.getSourceId(),userRolePermissions);
            }
        });

        for (String projectId : projectIds) {
            List<UserRolePermission> userRolePermissions = projectPermissionMap.get(projectId);
            if (CollectionUtils.isEmpty(userRolePermissions)) {
                continue;
            }
            for (UserRolePermission userRolePermission : userRolePermissions) {
                if (permissions.contains(userRolePermission.getPermissionId())) {
                    permissionProjectIdMap.computeIfAbsent(userRolePermission.getPermissionId(), key -> new LinkedHashSet<>()).add(projectId);
                }
            }
        }
        return permissionProjectIdMap;

    }






}
