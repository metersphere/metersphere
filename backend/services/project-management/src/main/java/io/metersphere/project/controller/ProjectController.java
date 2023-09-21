package io.metersphere.project.controller;

import io.metersphere.project.domain.Project;
import io.metersphere.project.request.ProjectSwitchRequest;
import io.metersphere.project.service.ProjectLogService;
import io.metersphere.project.service.ProjectService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.ProjectDTO;
import io.metersphere.sdk.dto.UpdateProjectRequest;
import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.system.domain.TestResourcePool;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.utils.SessionUtils;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "项目管理")
@RequestMapping("/project")
public class ProjectController {
    @Resource
    private ProjectService projectService;

    @GetMapping("/get/{id}")
    @Operation(summary = "项目管理-基本信息")
    @RequiresPermissions(PermissionConstants.PROJECT_BASE_INFO_READ)
    public ProjectDTO getProject(@PathVariable String id) {
        return projectService.getProjectById(id);
    }

    @GetMapping("/list/options/{organizationId}")
    @Operation(summary = "根据组织ID获取所有有权限的项目")
    @RequiresPermissions(PermissionConstants.PROJECT_BASE_INFO_READ)
    public List<Project> getUserProject(@PathVariable String organizationId) {
        return projectService.getUserProject(organizationId, SessionUtils.getUserId());
    }

    @PostMapping("/switch")
    @Operation(summary = "切换项目")
    @RequiresPermissions(PermissionConstants.PROJECT_BASE_INFO_READ)
    public UserDTO switchProject(@RequestBody ProjectSwitchRequest request) {
        return projectService.switchProject(request, SessionUtils.getUserId());
    }

    @PostMapping("/update")
    @Operation(summary = "项目管理-更新项目")
    @RequiresPermissions(PermissionConstants.PROJECT_BASE_INFO_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", msClass = ProjectLogService.class)
    public ProjectDTO updateProject(@RequestBody @Validated({Updated.class}) UpdateProjectRequest request) {
        return projectService.update(request, SessionUtils.getUserId());
    }

    @GetMapping("/pool-options/{type}/{projectId}")
    @Operation(summary = "项目管理-获取项目下的资源池")
    @RequiresPermissions(PermissionConstants.PROJECT_BASE_INFO_READ)
    public List<TestResourcePool> getPoolOptions(@PathVariable String type ,@PathVariable String projectId) {
        return projectService.getPoolOptions(projectId, type);
    }

}
