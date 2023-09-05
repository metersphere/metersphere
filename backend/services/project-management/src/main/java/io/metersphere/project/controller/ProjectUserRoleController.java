package io.metersphere.project.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.project.request.ProjectUserRoleEditRequest;
import io.metersphere.project.request.ProjectUserRoleMemberEditRequest;
import io.metersphere.project.request.ProjectUserRoleMemberRequest;
import io.metersphere.project.service.ProjectUserRoleLogService;
import io.metersphere.project.service.ProjectUserRoleService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.PermissionDefinitionItem;
import io.metersphere.sdk.dto.UserExtend;
import io.metersphere.sdk.dto.request.PermissionSettingUpdateRequest;
import io.metersphere.sdk.log.annotation.Log;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.PageUtils;
import io.metersphere.sdk.util.Pager;
import io.metersphere.sdk.util.SessionUtils;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "项目管理-项目与权限-用户组")
@RestController
@RequestMapping("/user/role/project")
public class ProjectUserRoleController {

    @Resource
    ProjectUserRoleService projectUserRoleService;

    @GetMapping("/list/{projectId}")
    @Operation(summary = "项目管理-项目与权限-用户组-获取用户组列表")
    @Parameter(name = "projectId", description = "当前项目ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @RequiresPermissions(PermissionConstants.PROJECT_GROUP_READ)
    public List<UserRole> list(@PathVariable String projectId) {
        return projectUserRoleService.list(projectId);
    }

    @PostMapping("/add")
    @Operation(summary = "项目管理-项目与权限-用户组-添加用户组")
    @RequiresPermissions(PermissionConstants.PROJECT_GROUP_ADD)
    @Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#request)", msClass = ProjectUserRoleLogService.class)
    public UserRole add(@Validated @RequestBody ProjectUserRoleEditRequest request) {
        UserRole userRole = new UserRole();
        userRole.setCreateUser(SessionUtils.getUserId());
        BeanUtils.copyBean(userRole, request);
        return projectUserRoleService.add(userRole);
    }

    @PostMapping("/update")
    @Operation(summary = "项目管理-项目与权限-用户组-修改用户组")
    @RequiresPermissions(PermissionConstants.PROJECT_GROUP_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", msClass = ProjectUserRoleLogService.class)
    public UserRole update(@Validated @RequestBody ProjectUserRoleEditRequest request) {
        UserRole userRole = new UserRole();
        BeanUtils.copyBean(userRole, request);
        return projectUserRoleService.update(userRole);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "项目管理-项目与权限-用户组-删除用户组")
    @RequiresPermissions(PermissionConstants.PROJECT_GROUP_DELETE)
    @Parameter(name = "id", description = "用户组ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = ProjectUserRoleLogService.class)
    public void delete(@PathVariable String id) {
        projectUserRoleService.delete(id, SessionUtils.getUserId());
    }

    @GetMapping("/permission/setting/{id}")
    @Operation(summary = "项目管理-项目与权限-用户组-获取用户组对应的权限配置")
    @Parameter(name = "id", description = "用户组ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @RequiresPermissions(PermissionConstants.PROJECT_GROUP_READ)
    public List<PermissionDefinitionItem> getPermissionSetting(@PathVariable String id) {
        return projectUserRoleService.getPermissionSetting(id);
    }

    @PostMapping("/permission/update")
    @Operation(summary = "项目管理-项目与权限-用户组-修改用户组对应的权限配置")
    @RequiresPermissions(PermissionConstants.PROJECT_GROUP_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updatePermissionSettingLog(#request)", msClass = ProjectUserRoleLogService.class)
    public void updatePermissionSetting(@Validated @RequestBody PermissionSettingUpdateRequest request) {
        projectUserRoleService.updatePermissionSetting(request);
    }

    @GetMapping("/get-member/option/{projectId}/{roleId}")
    @Operation(summary = "项目管理-项目与权限-用户组-获取成员下拉选项")
    @Parameters({
            @Parameter(name = "projectId", description = "当前项目ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED)),
            @Parameter(name = "roleId", description = "用户组ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    })
    @RequiresPermissions(value = {PermissionConstants.PROJECT_GROUP_READ})
    public List<UserExtend> getMember(@PathVariable String projectId, @PathVariable String roleId) {
        return projectUserRoleService.getMember(projectId, roleId);
    }

    @PostMapping("/list-member")
    @Operation(summary = "项目管理-项目与权限-用户组-获取成员列表")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_GROUP_READ})
    public Pager<List<User>> listMember(@Validated @RequestBody ProjectUserRoleMemberRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        return PageUtils.setPageInfo(page, projectUserRoleService.listMember(request));
    }

    @PostMapping("/add-member")
    @Operation(summary = "项目管理-项目与权限-用户组-添加用户组成员")
    @RequiresPermissions(PermissionConstants.PROJECT_GROUP_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.editMemberLog(#request)", msClass = ProjectUserRoleLogService.class)
    public void addMember(@Validated @RequestBody ProjectUserRoleMemberEditRequest request) {
        projectUserRoleService.addMember(request, SessionUtils.getUserId());
    }

    @PostMapping("/remove-member")
    @Operation(summary = "项目管理-项目与权限-用户组-删除用户组成员")
    @RequiresPermissions(PermissionConstants.PROJECT_GROUP_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.editMemberLog(#request)", msClass = ProjectUserRoleLogService.class)
    public void removeMember(@Validated @RequestBody ProjectUserRoleMemberEditRequest request) {
        projectUserRoleService.removeMember(request);
    }
}
