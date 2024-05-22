package io.metersphere.project.controller;

import io.metersphere.project.domain.Project;
import io.metersphere.project.dto.ProjectRequest;
import io.metersphere.project.request.ProjectSwitchRequest;
import io.metersphere.project.service.ProjectLogService;
import io.metersphere.project.service.ProjectService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.ProjectDTO;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.user.UserDTO;
import io.metersphere.system.dto.user.UserExtendDTO;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.SessionUtils;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @CheckOwner(resourceId = "#id", resourceType = "project")
    public ProjectDTO getProject(@PathVariable String id) {
        return projectService.getProjectById(id);
    }

    @GetMapping("/list/options/{organizationId}")
    @Operation(summary = "根据组织ID获取所有有权限的项目")
    @CheckOwner(resourceId = "#organizationId", resourceType = "organization")
    public List<Project> getUserProject(@PathVariable String organizationId) {
        return projectService.getUserProject(organizationId, SessionUtils.getUserId());
    }

    @GetMapping("/list/options/{organizationId}/{module}")
    @Operation(summary = "根据组织ID获取所有开启某个模块的所有有权限的项目")
    @CheckOwner(resourceId = "#organizationId", resourceType = "organization")
    public List<Project> getUserProjectWidthModule(@PathVariable String organizationId, @PathVariable String module) {
        return projectService.getUserProjectWidthModule(organizationId, module, SessionUtils.getUserId());
    }

    @PostMapping("/switch")
    @Operation(summary = "切换项目")
    @RequiresPermissions(PermissionConstants.PROJECT_BASE_INFO_READ)
    @CheckOwner(resourceId = "#request.projectId", resourceType = "project")
    public UserDTO switchProject(@RequestBody ProjectSwitchRequest request) {
        return projectService.switchProject(request, SessionUtils.getUserId());
    }

    @PostMapping("/update")
    @Operation(summary = "项目管理-更新项目")
    @RequiresPermissions(PermissionConstants.PROJECT_BASE_INFO_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", msClass = ProjectLogService.class)
    @CheckOwner(resourceId = "#request.getId()", resourceType = "project")
    public ProjectDTO updateProject(@RequestBody @Validated({Updated.class}) ProjectRequest request) {
        return projectService.update(request, SessionUtils.getUserId());
    }

    @GetMapping("/pool-options/{type}/{projectId}")
    @Operation(summary = "项目管理-获取项目下的资源池")
    @RequiresPermissions(PermissionConstants.PROJECT_BASE_INFO_READ)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<OptionDTO> getPoolOptions(@PathVariable String type, @PathVariable String projectId) {
        return projectService.getPoolOptions(projectId);
    }

    @GetMapping("/has-permission/{id}")
    @Operation(summary = "项目管理-获取当前用户是否有当前项目的权限")
    @CheckOwner(resourceId = "#id", resourceType = "project")
    public boolean hasPermission(@PathVariable String id) {
        return projectService.hasPermission(id, SessionUtils.getUserId());
    }

    @GetMapping("/get-member/option/{projectId}")
    @Operation(summary = "项目管理-获取成员下拉选项")
    @RequiresPermissions(PermissionConstants.PROJECT_BASE_INFO_READ)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<UserExtendDTO> getMemberOption(@PathVariable String projectId,
                                               @Schema(description = "查询关键字，根据邮箱和用户名查询")
                                               @RequestParam(value = "keyword", required = false) String keyword) {
        return projectService.getMemberOption(projectId, keyword);
    }

}
