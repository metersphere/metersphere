package io.metersphere.system.controller;

import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.PermissionSettingDTO;
import io.metersphere.system.domain.UserRole;
import io.metersphere.system.dto.request.PermissionSettingUpdateRequest;
import io.metersphere.system.service.GlobalUserRoleService;
import io.metersphere.validation.groups.Created;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : jianxing
 * @date : 2023-6-9
 */
@Tag(name = "全局用户组")
@RestController
@RequestMapping("/user/role/global")
public class GlobalUserRoleController {

    @Resource
    private GlobalUserRoleService globalUserRoleService;

    @GetMapping("/list")
    @Operation(summary = "获取全局用户组列表")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_ROLE_READ)
    public List<UserRole> list() {
        return globalUserRoleService.list();
    }

    @GetMapping("/permission/setting/{id}")
    @Operation(summary = "获取全局用户组对应的权限配置")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_ROLE_READ)
    public List<PermissionSettingDTO> getPermissionSetting(@PathVariable String id) {
        return new ArrayList<>();
    }

    @PostMapping("/permission/update")
    @Operation(summary = "编辑全局用户组对应的权限配置")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_ROLE_UPDATE)
    public void updatePermissionSetting(@RequestBody PermissionSettingUpdateRequest request) {
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "获取单个全局用户组信息")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_ROLE_READ)
    public UserRole get(@PathVariable String id) {
        return globalUserRoleService.get(id);
    }

    @PostMapping("/add")
    @Operation(summary = "添加自定义全局用户组")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_ROLE_ADD)
    public UserRole add(@Validated({Created.class}) @RequestBody UserRole userRole) {
        return globalUserRoleService.add(userRole);
    }

    @PostMapping("/update")
    @Operation(summary = "更新自定义全局用户组")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_ROLE_UPDATE)
    public UserRole update(@Validated({Created.class}) @RequestBody UserRole userRole) {
        return globalUserRoleService.update(userRole);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除自定义全局用户组")
    @RequiresPermissions(PermissionConstants.SYSTEM_USER_ROLE_DELETE)
    public String delete(@PathVariable String id) {
        return globalUserRoleService.delete(id);
    }
}
