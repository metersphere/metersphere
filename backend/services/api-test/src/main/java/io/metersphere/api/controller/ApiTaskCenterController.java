package io.metersphere.api.controller;

import io.metersphere.api.service.ApiTaskCenterService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.taskcenter.TaskCenterDTO;
import io.metersphere.system.dto.taskcenter.request.OrganizationTaskCenterPageRequest;
import io.metersphere.system.dto.taskcenter.request.ProjectTaskCenterPageRequest;
import io.metersphere.system.dto.taskcenter.request.TaskCenterPageRequest;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.Pager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author: LAN
 * @date: 2024/1/17 19:19
 * @version: 1.0
 */
@RestController
@RequestMapping(value = "/task/center")
@Tag(name = "任务中心-实时任务-接口用例/场景")
public class ApiTaskCenterController {

    @Resource
    private ApiTaskCenterService apiTaskCenterService;


    @PostMapping("/api/project/real-time/page")
    @Operation(summary = "项目-任务中心-接口用例/场景-实时任务列表")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<TaskCenterDTO>> projectList(@Validated @RequestBody ProjectTaskCenterPageRequest request) {
        return apiTaskCenterService.getProjectPage(request);
    }

    @PostMapping("/api/org/real-time/page")
    @Operation(summary = "组织-任务中心-接口用例/场景-实时任务列表")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TASK_CENTER_READ)
    @CheckOwner(resourceId = "#request.getOrganizationId()", resourceType = "organization")
    public Pager<List<TaskCenterDTO>> orgList(@Validated @RequestBody OrganizationTaskCenterPageRequest request) {
        return apiTaskCenterService.getOrganizationPage(request);
    }

    @PostMapping("/api/system/real-time/page")
    @Operation(summary = "系统-任务中心-接口用例/场景-实时任务列表")
    @RequiresPermissions(PermissionConstants.SYSTEM_TASK_CENTER_READ)
    public Pager<List<TaskCenterDTO>> systemList(@Validated @RequestBody TaskCenterPageRequest request) {
        return apiTaskCenterService.getSystemPage(request);
    }

}
