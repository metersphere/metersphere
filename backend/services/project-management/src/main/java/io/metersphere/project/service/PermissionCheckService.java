package io.metersphere.project.service;


import io.metersphere.sdk.constants.InternalUserRole;
import io.metersphere.sdk.constants.UserRoleType;
import io.metersphere.system.domain.UserRole;
import io.metersphere.system.domain.UserRolePermission;
import io.metersphere.system.dto.user.UserDTO;
import io.metersphere.system.service.UserLoginService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class PermissionCheckService {
    @Resource
    private UserLoginService userLoginService;

    public boolean userHasProjectPermission(String userId, String projectId, String permission) {
        Map<String, List<UserRolePermission>> userRolePermissions = new HashMap<>();
        Map<String, UserRole> role = new HashMap<>();
        UserDTO user = userLoginService.getUserDTO(userId);
        if (user != null) {
            user.getUserRoleRelations().forEach(ug -> user.getUserRolePermissions().forEach(gp -> {
                if (StringUtils.equalsIgnoreCase(gp.getUserRole().getId(), ug.getRoleId())) {
                    userRolePermissions.put(ug.getId(), gp.getUserRolePermissions());
                    role.put(ug.getId(), gp.getUserRole());
                }
            }));
            // 判断是否是超级管理员
            long count = user.getUserRoles()
                    .stream()
                    .filter(g -> StringUtils.equalsIgnoreCase(g.getId(), InternalUserRole.ADMIN.getValue()))
                    .count();
            if (count > 0) {
                return true;
            }
            Set<String> currentProjectPermissions = user.getUserRoleRelations().stream()
                    .filter(ug -> role.get(ug.getId()) != null && StringUtils.equalsIgnoreCase(role.get(ug.getId()).getType(), UserRoleType.PROJECT.name()))
                    .filter(ug -> StringUtils.equalsIgnoreCase(ug.getSourceId(), projectId))
                    .flatMap(ug -> userRolePermissions.get(ug.getId()).stream())
                    .map(UserRolePermission::getPermissionId)
                    .collect(Collectors.toSet());
            return currentProjectPermissions.contains(permission);
        }
        return false;
    }


}
