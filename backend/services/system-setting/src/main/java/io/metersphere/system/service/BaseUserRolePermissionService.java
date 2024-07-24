package io.metersphere.system.service;

import io.metersphere.system.domain.UserRolePermission;
import io.metersphere.system.domain.UserRolePermissionExample;
import io.metersphere.system.dto.sdk.request.PermissionSettingUpdateRequest;
import io.metersphere.system.mapper.UserRolePermissionMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户组与权限的关联关系
 *
 * @author jianxing
 * @date : 2023-6-8
 */
@Service
public class BaseUserRolePermissionService {
    @Resource
    private UserRolePermissionMapper userRolePermissionMapper;

    /**
     * 查询用户组对应的权限列表
     *
     * @param roleId
     * @return
     */
    public List<UserRolePermission> getByRoleId(String roleId) {
        UserRolePermissionExample example = new UserRolePermissionExample();
        example.createCriteria().andRoleIdEqualTo(roleId);
        return userRolePermissionMapper.selectByExample(example);
    }

    /**
     * 查询用户组对应的权限ID
     *
     * @param roleId
     * @return
     */
    public Set<String> getPermissionIdSetByRoleId(String roleId) {
        return getByRoleId(roleId).stream()
                .map(UserRolePermission::getPermissionId)
                .collect(Collectors.toSet());
    }

    /**
     * 更新单个用户组的配置项
     *
     * @param request
     */
    public void updatePermissionSetting(PermissionSettingUpdateRequest request) {
        List<PermissionSettingUpdateRequest.PermissionUpdateRequest> permissions = request.getPermissions();

        // 先删除 (排除内置基本信息用户组)
        UserRolePermissionExample userGroupPermissionExample = new UserRolePermissionExample();
        userGroupPermissionExample.createCriteria()
                .andRoleIdEqualTo(request.getUserRoleId()).andPermissionIdNotEqualTo("PROJECT_BASE_INFO:READ");
        userRolePermissionMapper.deleteByExample(userGroupPermissionExample);

        // 再新增
        String groupId = request.getUserRoleId();
        permissions.forEach(permission -> {
            if (BooleanUtils.isTrue(permission.getEnable())) {
                String permissionId = permission.getId();
                UserRolePermission groupPermission = new UserRolePermission();
                groupPermission.setId(IDGenerator.nextStr());
                groupPermission.setRoleId(groupId);
                groupPermission.setPermissionId(permissionId);
                userRolePermissionMapper.insert(groupPermission);
            }
        });
    }

    public void deleteByRoleId(String roleId) {
        UserRolePermissionExample example = new UserRolePermissionExample();
        example.createCriteria().andRoleIdEqualTo(roleId);
        userRolePermissionMapper.deleteByExample(example);
    }
}
