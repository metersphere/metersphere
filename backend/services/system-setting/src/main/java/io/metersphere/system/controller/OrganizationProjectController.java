package io.metersphere.system.controller;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.AddProjectRequest;
import io.metersphere.system.dto.ProjectDTO;
import io.metersphere.system.dto.UpdateProjectNameRequest;
import io.metersphere.system.dto.UpdateProjectRequest;
import io.metersphere.system.dto.request.*;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.user.UserExtendDTO;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.service.OrganizationProjectLogService;
import io.metersphere.system.service.OrganizationProjectService;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "系统设置-组织-项目")
@RequestMapping("/organization/project")
public class OrganizationProjectController {
    @Resource
    private OrganizationProjectService organizationProjectService;

    @PostMapping("/add")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_PROJECT_READ_ADD)
    @Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#request)", msClass = OrganizationProjectLogService.class)
    @Operation(summary = "系统设置-组织-项目-创建项目")
    public ProjectDTO addProject(@RequestBody @Validated({Created.class}) AddProjectRequest request) {
        return organizationProjectService.add(request, SessionUtils.getUserId());
    }


    @GetMapping("/get/{id}")
    @Operation(summary = "系统设置-组织-项目-根据ID获取项目信息")
    @Parameter(name = "id", description = "项目id", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @RequiresPermissions(PermissionConstants.ORGANIZATION_PROJECT_READ)
    @CheckOwner(resourceId = "#id", resourceType = "project")
    public ProjectDTO getProject(@PathVariable @NotBlank String id) {
        return organizationProjectService.get(id);
    }

    @PostMapping("/page")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_PROJECT_READ)
    @Operation(summary = "系统设置-组织-项目-获取项目列表")
    @CheckOwner(resourceId = "#request.getOrganizationId()", resourceType = "organization")
    public Pager<List<ProjectDTO>> getProjectList(@Validated @RequestBody OrganizationProjectRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, organizationProjectService.getProjectList(request));
    }

    @PostMapping("/update")
    @Operation(summary = "系统设置-组织-项目-编辑")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_PROJECT_READ_UPDATE)
    @CheckOwner(resourceId = "#request.id", resourceType = "project")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", msClass = OrganizationProjectLogService.class)
    public ProjectDTO updateProject(@RequestBody @Validated({Updated.class}) UpdateProjectRequest request) {
        return organizationProjectService.update(request, SessionUtils.getUserId());
    }

    @GetMapping("/delete/{id}")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_PROJECT_READ_DELETE)
    @Operation(summary = "系统设置-组织-项目-删除")
    @Parameter(name = "id", description = "项目", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = OrganizationProjectLogService.class)
    @CheckOwner(resourceId = "#id", resourceType = "project")
    public int deleteProject(@PathVariable String id) {
        return organizationProjectService.delete(id, SessionUtils.getUserId());
    }

    @GetMapping("/revoke/{id}")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_PROJECT_READ_RECOVER)
    @Operation(summary = "系统设置-组织-项目-撤销删除")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.recoverLog(#id)", msClass = OrganizationProjectLogService.class)
    @Parameter(name = "id", description = "项目", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @CheckOwner(resourceId = "#id", resourceType = "project")
    public int revokeProject(@PathVariable String id) {
        return organizationProjectService.revoke(id, SessionUtils.getUserId());
    }

    @GetMapping("/enable/{id}")
    @Operation(summary = "系统设置-组织-项目-启用")
    @Parameter(name = "id", description = "项目ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#id)", msClass = OrganizationProjectLogService.class)
    @RequiresPermissions(PermissionConstants.ORGANIZATION_PROJECT_READ_UPDATE)
    @CheckOwner(resourceId = "#id", resourceType = "project")
    public void enable(@PathVariable String id) {
        organizationProjectService.enable(id, SessionUtils.getUserId());
    }

    @GetMapping("/disable/{id}")
    @Operation(summary = "系统设置-组织-项目-禁用")
    @Parameter(name = "id", description = "项目ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @RequiresPermissions(PermissionConstants.ORGANIZATION_PROJECT_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#id)", msClass = OrganizationProjectLogService.class)
    @CheckOwner(resourceId = "#id", resourceType = "project")
    public void disable(@PathVariable String id) {
        organizationProjectService.disable(id, SessionUtils.getUserId());
    }

    @PostMapping("/member-list")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_PROJECT_READ)
    @Operation(summary = "系统设置-组织-项目-成员列表")
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<UserExtendDTO>> getProjectMember(@Validated @RequestBody ProjectMemberRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        return PageUtils.setPageInfo(page, organizationProjectService.getProjectMember(request));
    }

    @PostMapping("/add-members")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_PROJECT_MEMBER_ADD)
    @Operation(summary = "系统设置-组织-项目-添加成员")
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void addProjectMember(@Validated @RequestBody ProjectAddMemberRequest request) {
        organizationProjectService.orgAddProjectMember(request, SessionUtils.getUserId());
    }

    @GetMapping("/remove-member/{projectId}/{userId}")
    @Operation(summary = "系统设置-组织-项目-移除成员")
    @Parameter(name = "userId", description = "用户id", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @Parameter(name = "projectId", description = "项目id", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @RequiresPermissions(PermissionConstants.ORGANIZATION_PROJECT_MEMBER_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#projectId)", msClass = OrganizationProjectLogService.class)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public int removeProjectMember(@PathVariable String projectId, @PathVariable String userId) {
        return organizationProjectService.removeProjectMember(projectId, userId, SessionUtils.getUserId());
    }

    @GetMapping("/user-admin-list/{organizationId}")
    @Operation(summary = "系统设置-组织-项目-获取项目管理员下拉选项")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_PROJECT_READ)
    @CheckOwner(resourceId = "#organizationId", resourceType = "organization")
    public List<UserExtendDTO> getUserAdminList(@PathVariable String organizationId, @Schema(description = "查询关键字，根据邮箱和用户名查询")
    @RequestParam(value = "keyword", required = false) String keyword) {
        return organizationProjectService.getUserAdminList(organizationId, keyword);
    }

    @GetMapping("/user-member-list/{organizationId}/{projectId}")
    @Operation(summary = "系统设置-组织-项目-获取成员列表")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_PROJECT_READ)
    @CheckOwner(resourceId = "#organizationId", resourceType = "organization")
    public List<UserExtendDTO> getUserMemberList(@PathVariable String organizationId, @PathVariable String projectId,
                                                 @Schema(description = "查询关键字，根据邮箱和用户名查询")
                                                 @RequestParam(value = "keyword", required = false) String keyword) {
        return organizationProjectService.getUserMemberList(organizationId, projectId, keyword);
    }

    @PostMapping("/pool-options")
    @Operation(summary = "系统设置-组织-项目-获取资源池下拉选项")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_PROJECT_READ)
    @CheckOwner(resourceId = "#request.getOrganizationId()", resourceType = "organization")
    public List<OptionDTO> getProjectOptions(@Validated @RequestBody ProjectPoolRequest request) {
        return organizationProjectService.getTestResourcePoolOptions(request);
    }

    @PostMapping("/rename")
    @Operation(summary = "系统设置-组织-项目-修改项目名称")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_PROJECT_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.renameLog(#request)", msClass = OrganizationProjectLogService.class)
    @CheckOwner(resourceId = "#request.getId()", resourceType = "project")
    public void rename(@RequestBody @Validated({Updated.class}) UpdateProjectNameRequest request) {
        organizationProjectService.rename(request, SessionUtils.getUserId());
    }

    @PostMapping("/user-list")
    @Operation(summary = "系统设置-组织-项目-分页获取成员列表")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    public Pager<List<UserExtendDTO>> getMemberList(@Validated @RequestBody ProjectUserRequest request) {
        return organizationProjectService.getMemberList(request);
    }

}
