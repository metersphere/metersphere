package io.metersphere.system.controller;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.project.domain.Project;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.AddProjectRequest;
import io.metersphere.sdk.dto.ProjectDTO;
import io.metersphere.sdk.dto.UpdateProjectRequest;
import io.metersphere.sdk.log.annotation.Log;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.util.PageUtils;
import io.metersphere.sdk.util.Pager;
import io.metersphere.sdk.util.SessionUtils;
import io.metersphere.system.dto.UserExtend;
import io.metersphere.system.request.ProjectAddMemberRequest;
import io.metersphere.system.request.ProjectMemberRequest;
import io.metersphere.system.request.ProjectRequest;
import io.metersphere.system.service.SystemProjectService;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/project")
public class SystemProjectController {
    @Resource
    private SystemProjectService systemProjectService;

    @PostMapping("/add")
    @RequiresPermissions(PermissionConstants.SYSTEM_PROJECT_READ_ADD)
    @Log(type = OperationLogType.ADD, module = OperationLogModule.SYSTEM_PROJECT, details = "#project.name")
    @Operation(summary = "添加项目")
    public Project addProject(@RequestBody @Validated({Created.class}) AddProjectRequest project) {
        return systemProjectService.add(project, SessionUtils.getUserId());
    }


    @GetMapping("/get/{id}")
    @Operation(summary = "根据ID获取项目信息")
    @RequiresPermissions(PermissionConstants.SYSTEM_PROJECT_READ)
    public Project getProject(@PathVariable @NotBlank String id) {
        return systemProjectService.get(id);
    }

    @PostMapping("/page")
    @RequiresPermissions(PermissionConstants.SYSTEM_PROJECT_READ)
    @Operation(summary = "获取项目列表")
    public Pager<List<ProjectDTO>> getProjectList(@Validated @RequestBody ProjectRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, systemProjectService.getProjectList(request));
    }

    @PostMapping("/update")
    @Log(type = OperationLogType.UPDATE, module = OperationLogModule.SYSTEM_PROJECT,
            sourceId = "#project.id", details = "#project.name")
    @Operation(summary = "更新项目信息")
    @RequiresPermissions(PermissionConstants.SYSTEM_PROJECT_READ_UPDATE)
    public Project updateProject(@RequestBody @Validated({Updated.class}) UpdateProjectRequest project) {
        return systemProjectService.update(project, SessionUtils.getUserId());
    }

    @GetMapping("/delete/{id}")
    @RequiresPermissions(PermissionConstants.SYSTEM_PROJECT_READ_DELETE)
    @Operation(summary = "删除项目")
    @Log(isBefore = true, type = OperationLogType.DELETE, module = OperationLogModule.SYSTEM_PROJECT,
            details = "#msClass.getLogDetails(#id)", msClass = SystemProjectService.class, sourceId = "#id")
    public int deleteProject(@PathVariable String id) {
        return systemProjectService.delete(id, SessionUtils.getUserId());
    }

    @GetMapping("/revoke/{id}")
    @RequiresPermissions(PermissionConstants.SYSTEM_PROJECT_READ_DELETE)
    @Log(isBefore = true, type = OperationLogType.UPDATE, module = OperationLogModule.SYSTEM_PROJECT,
            details = "#msClass.getLogDetails(#id)", msClass = SystemProjectService.class, sourceId = "#id")
    public int revokeProject(@PathVariable String id) {
       return systemProjectService.revoke(id);
    }

    @PostMapping("/member-list")
    @RequiresPermissions(PermissionConstants.SYSTEM_PROJECT_READ_ADD_USER)
    @Operation(summary = "获取项目下成员列表")
    public Pager<List<UserExtend>> getProjectMember(@Validated @RequestBody ProjectMemberRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, systemProjectService.getProjectMember(request));
    }

    @PostMapping("/add-member")
    @RequiresPermissions(PermissionConstants.SYSTEM_PROJECT_READ_ADD_USER)
    @Operation(summary = "添加项目成员")
    public void addProjectMember(@Validated @RequestBody ProjectAddMemberRequest request) {
        systemProjectService.addProjectMember(request, SessionUtils.getUserId(), false);
    }

    @GetMapping("/remove-member/{projectId}/{userId}")
    @Operation(summary = "移除项目成员")
    @RequiresPermissions(PermissionConstants.SYSTEM_PROJECT_READ_DELETE_USER)
    @Log(isBefore = true, type = OperationLogType.DELETE, module = OperationLogModule.SYSTEM_PROJECT_MEMBER, sourceId = "#projectId",
            details = "#msClass.getLogs(#userId)", msClass = SystemProjectService.class)
    public int removeProjectMember(@PathVariable String projectId, @PathVariable String userId) {
        return systemProjectService.removeProjectMember(projectId, userId);
    }


}
