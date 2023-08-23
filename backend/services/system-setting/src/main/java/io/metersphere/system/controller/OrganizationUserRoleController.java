package io.metersphere.system.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.PermissionDefinitionItem;
import io.metersphere.sdk.dto.request.PermissionSettingUpdateRequest;
import io.metersphere.sdk.log.annotation.Log;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.PageUtils;
import io.metersphere.sdk.util.Pager;
import io.metersphere.sdk.util.SessionUtils;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserRole;
import io.metersphere.system.dto.UserExtend;
import io.metersphere.system.request.OrganizationUserRoleEditRequest;
import io.metersphere.system.request.OrganizationUserRoleMemberEditRequest;
import io.metersphere.system.request.OrganizationUserRoleMemberRequest;
import io.metersphere.system.service.OrganizationUserRoleLogService;
import io.metersphere.system.service.OrganizationUserRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author song-cc-rock
 */
@Tag(name = "组织-用户组与权限")
@RestController
@RequestMapping("/user/role/organization")
public class OrganizationUserRoleController {

    @Resource
    OrganizationUserRoleService organizationUserRoleService;

    @GetMapping("/list/{organizationId}")
    @Operation(summary = "获取组织用户组列表")
    @Parameter(name = "organizationId", description = "当前组织ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @RequiresPermissions(PermissionConstants.ORGANIZATION_USER_ROLE_READ)
    public List<UserRole> list(@PathVariable String organizationId) {
        return organizationUserRoleService.list(organizationId);
    }

    @PostMapping("/add")
    @Operation(summary = "添加组织用户组")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_USER_ROLE_READ_ADD)
    @Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#request)", msClass = OrganizationUserRoleLogService.class)
    public UserRole add(@Validated @RequestBody OrganizationUserRoleEditRequest request) {
        UserRole userRole = new UserRole();
        userRole.setCreateUser(SessionUtils.getUserId());
        BeanUtils.copyBean(userRole, request);
        return organizationUserRoleService.add(userRole);
    }

    @PostMapping("/update")
    @Operation(summary = "修改组织用户组")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_USER_ROLE_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", msClass = OrganizationUserRoleLogService.class)
    public UserRole update(@Validated @RequestBody OrganizationUserRoleEditRequest request) {
        UserRole userRole = new UserRole();
        BeanUtils.copyBean(userRole, request);
        return organizationUserRoleService.update(userRole);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除组织用户组")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_USER_ROLE_READ_DELETE)
    @Parameter(name = "id", description = "用户组ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = OrganizationUserRoleLogService.class)
    public void delete(@PathVariable String id) {
        organizationUserRoleService.delete(id, SessionUtils.getUserId());
    }

    @GetMapping("/permission/setting/{id}")
    @Operation(summary = "获取组织用户组对应的权限配置")
    @Parameter(name = "id", description = "用户组ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @RequiresPermissions(PermissionConstants.ORGANIZATION_USER_ROLE_READ)
    public List<PermissionDefinitionItem> getPermissionSetting(@PathVariable String id) {
        return organizationUserRoleService.getPermissionSetting(id);
    }

    @PostMapping("/permission/update")
    @Operation(summary = "编辑组织用户组对应的权限配置")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_USER_ROLE_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updatePermissionSettingLog(#request)", msClass = OrganizationUserRoleLogService.class)
    public void updatePermissionSetting(@Validated @RequestBody PermissionSettingUpdateRequest request) {
        organizationUserRoleService.updatePermissionSetting(request);
    }

    @GetMapping("/get-member/option/{organizationId}/{roleId}")
    @Operation(summary = "获取组织用户组-成员下拉选项")
    @RequiresPermissions(value = {PermissionConstants.ORGANIZATION_USER_ROLE_READ})
    public List<UserExtend> getMember(@PathVariable String organizationId, @PathVariable String roleId) {
        return organizationUserRoleService.getMember(organizationId, roleId);
    }

    @PostMapping("/list-member")
    @Operation(summary = "获取组织用户组-成员")
    @RequiresPermissions(value = {PermissionConstants.ORGANIZATION_USER_ROLE_READ})
    public Pager<List<User>> listMember(@Validated @RequestBody OrganizationUserRoleMemberRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        return PageUtils.setPageInfo(page, organizationUserRoleService.listMember(request));
    }

    @PostMapping("/add-member")
    @Operation(summary = "添加组织用户组成员")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_USER_ROLE_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.editMemberLog(#request)", msClass = OrganizationUserRoleLogService.class)
    public void addMember(@Validated @RequestBody OrganizationUserRoleMemberEditRequest request) {
        organizationUserRoleService.addMember(request, SessionUtils.getUserId());
    }

    @PostMapping("/remove-member")
    @Operation(summary = "删除组织用户组成员")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_USER_ROLE_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.editMemberLog(#request)", msClass = OrganizationUserRoleLogService.class)
    public void removeMember(@Validated @RequestBody OrganizationUserRoleMemberEditRequest request) {
        organizationUserRoleService.removeMember(request);
    }
}
