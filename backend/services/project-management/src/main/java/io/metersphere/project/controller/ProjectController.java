package io.metersphere.project.controller;

import io.metersphere.project.domain.Project;
import io.metersphere.project.request.ProjectSwitchRequest;
import io.metersphere.project.service.ProjectService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.ProjectExtendDTO;
import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.sdk.util.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "项目管理")
@RequestMapping("/project")
public class ProjectController {
    @Resource
    private ProjectService projectService;

    @GetMapping("/get/{id}")
    @Operation(summary = "基本信息")
    @RequiresPermissions(PermissionConstants.PROJECT_BASE_INFO_READ)
    public ProjectExtendDTO getProject(@PathVariable String id) {
        return projectService.getProjectById(id);
    }

    @GetMapping("/list/options/{organizationId}")
    @Operation(summary = "根据项目ID获取所有有权限的项目")
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
}
