package io.metersphere.system.controller;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.OptionDTO;
import io.metersphere.sdk.dto.UserExtend;
import io.metersphere.sdk.util.PageUtils;
import io.metersphere.sdk.util.Pager;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.AddProjectRequest;
import io.metersphere.system.dto.ProjectDTO;
import io.metersphere.system.dto.UpdateProjectRequest;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.request.ProjectAddMemberBatchRequest;
import io.metersphere.system.request.ProjectAddMemberRequest;
import io.metersphere.system.request.ProjectMemberRequest;
import io.metersphere.system.request.ProjectRequest;
import io.metersphere.system.service.SystemProjectLogService;
import io.metersphere.system.service.SystemProjectService;
import io.metersphere.system.service.UserService;
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
@Tag(name = "系统设置-系统-组织与项目-项目")
@RequestMapping("/system/project")
public class SystemProjectController {
    @Resource
    private SystemProjectService systemProjectService;
    @Resource
    private UserService userService;

    @PostMapping("/add")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_ADD)
    @Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#project)", msClass = SystemProjectLogService.class)
    @Operation(summary = "系统设置-系统-组织与项目-项目-创建项目")
    public ProjectDTO addProject(@RequestBody @Validated({Created.class}) AddProjectRequest project) {
        return systemProjectService.add(project, SessionUtils.getUserId());
    }


    @GetMapping("/get/{id}")
    @Operation(summary = "系统设置-系统-组织与项目-项目-根据ID获取项目信息")
    @Parameter(name = "id", description = "项目id", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    public ProjectDTO getProject(@PathVariable @NotBlank String id) {
        return systemProjectService.get(id);
    }

    @PostMapping("/page")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    @Operation(summary = "系统设置-系统-组织与项目-项目-获取项目列表")
    public Pager<List<ProjectDTO>> getProjectList(@Validated @RequestBody ProjectRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, systemProjectService.getProjectList(request));
    }

    @PostMapping("/update")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#project)", msClass = SystemProjectLogService.class)
    @Operation(summary = "系统设置-系统-组织与项目-项目-编辑")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_UPDATE)
    public ProjectDTO updateProject(@RequestBody @Validated({Updated.class}) UpdateProjectRequest project) {
        return systemProjectService.update(project, SessionUtils.getUserId());
    }

    @GetMapping("/delete/{id}")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_DELETE)
    @Operation(summary = "系统设置-系统-组织与项目-项目-删除")
    @Parameter(name = "id", description = "项目", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = SystemProjectLogService.class)
    public int deleteProject(@PathVariable String id) {
        return systemProjectService.delete(id, SessionUtils.getUserId());
    }

    @GetMapping("/revoke/{id}")
    @Operation(summary = "系统设置-系统-组织与项目-项目-撤销删除")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_RECOVER)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.recoverLog(#id)", msClass = SystemProjectLogService.class)
    @Parameter(name = "id", description = "项目", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    public int revokeProject(@PathVariable String id) {
       return systemProjectService.revoke(id, SessionUtils.getUserId());
    }

    @GetMapping("/enable/{id}")
    @Operation(summary = "系统设置-系统-组织与项目-项目-启用")
    @Parameter(name = "id", description = "项目ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#id)", msClass = SystemProjectLogService.class)
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_UPDATE)
    public void enable(@PathVariable String id) {
        systemProjectService.enable(id, SessionUtils.getUserId());
    }

    @GetMapping("/disable/{id}")
    @Operation(summary = "系统设置-系统-组织与项目-项目-禁用")
    @Parameter(name = "id", description = "项目ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#id)", msClass = SystemProjectLogService.class)
    public void disable(@PathVariable String id) {
        systemProjectService.disable(id, SessionUtils.getUserId());
    }

    @PostMapping("/member-list")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    @Operation(summary = "系统设置-系统-组织与项目-项目-成员列表")
    public Pager<List<UserExtend>> getProjectMember(@Validated @RequestBody ProjectMemberRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, systemProjectService.getProjectMember(request));
    }

    @PostMapping("/add-member")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_MEMBER_ADD)
    @Operation(summary = "系统设置-系统-组织与项目-项目-添加成员")
    public void addProjectMember(@Validated @RequestBody ProjectAddMemberRequest request) {
        ProjectAddMemberBatchRequest batchRequest = new ProjectAddMemberBatchRequest();
        batchRequest.setProjectIds(List.of(request.getProjectId()));
        batchRequest.setUserIds(request.getUserIds());
        systemProjectService.addProjectMember(batchRequest, SessionUtils.getUserId());
    }

    @GetMapping("/remove-member/{projectId}/{userId}")
    @Operation(summary = "系统设置-系统-组织与项目-项目-移除成员")
    @Parameter(name = "userId", description = "用户id", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @Parameter(name = "projectId", description = "项目id", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_MEMBER_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#projectId)", msClass = SystemProjectLogService.class)
    public int removeProjectMember(@PathVariable String projectId, @PathVariable String userId) {
        return systemProjectService.removeProjectMember(projectId, userId, SessionUtils.getUserId());
    }

    @GetMapping("/user-list")
    @Operation(summary = "系统设置-系统-组织与项目-项目-系统-组织及项目, 获取管理员下拉选项")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    public List<User> getUserList(@Schema(description = "查询关键字，根据邮箱和用户名查询")
                                      @RequestParam(value = "keyword", required = false) String keyword) {
        return userService.getUserList(keyword);
    }

    @GetMapping("/pool-options")
    @Operation(summary = "系统设置-系统-组织与项目-项目-获取资源池下拉选项")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    public List<OptionDTO> getProjectOptions(@RequestParam(value = "organizationId",required = false) String organizationId) {
        return systemProjectService.getTestResourcePoolOptions(organizationId);
    }

}
