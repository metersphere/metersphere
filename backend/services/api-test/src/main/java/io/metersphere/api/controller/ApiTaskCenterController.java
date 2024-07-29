package io.metersphere.api.controller;

import io.metersphere.api.service.ApiTaskCenterService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.taskcenter.TaskCenterDTO;
import io.metersphere.system.dto.taskcenter.request.TaskCenterBatchRequest;
import io.metersphere.system.dto.taskcenter.request.TaskCenterPageRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    private static final String PROJECT = "project";

    @PostMapping("/api/project/real-time/page")
    @Operation(summary = "项目-任务中心-接口用例/场景-实时任务列表")
    public Pager<List<TaskCenterDTO>> projectList(@Validated @RequestBody TaskCenterPageRequest request) {
        return apiTaskCenterService.getProjectPage(request, SessionUtils.getCurrentProjectId());
    }

    @PostMapping("/api/org/real-time/page")
    @Operation(summary = "组织-任务中心-接口用例/场景-实时任务列表")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TASK_CENTER_READ)
    public Pager<List<TaskCenterDTO>> orgList(@Validated @RequestBody TaskCenterPageRequest request) {
        return apiTaskCenterService.getOrganizationPage(request, SessionUtils.getCurrentOrganizationId());
    }

    @PostMapping("/api/system/real-time/page")
    @Operation(summary = "系统-任务中心-接口用例/场景-实时任务列表")
    @RequiresPermissions(PermissionConstants.SYSTEM_TASK_CENTER_READ)
    public Pager<List<TaskCenterDTO>> systemList(@Validated @RequestBody TaskCenterPageRequest request) {
        return apiTaskCenterService.getSystemPage(request);
    }

    @PostMapping("/api/system/stop")
    @Operation(summary = "系统-任务中心-接口用例/场景-停止任务")
    public void systemStop(@Validated @RequestBody TaskCenterBatchRequest request) {
        apiTaskCenterService.systemStop(request, SessionUtils.getUserId());
    }

    @PostMapping("/api/org/stop")
    @Operation(summary = "组织-任务中心-接口用例/场景-停止任务")
    public void orgStop(@Validated @RequestBody TaskCenterBatchRequest request) {
        apiTaskCenterService.orgStop(request, SessionUtils.getCurrentOrganizationId(), SessionUtils.getUserId());
    }

    @PostMapping("/api/project/stop")
    @Operation(summary = "项目-任务中心-接口用例/场景-停止任务")
    public void projectStop(@Validated @RequestBody TaskCenterBatchRequest request) {
        apiTaskCenterService.hasPermission(PROJECT, request.getModuleType(),
                SessionUtils.getCurrentOrganizationId(),
                SessionUtils.getCurrentProjectId());
        apiTaskCenterService.projectStop(request, SessionUtils.getCurrentProjectId(), SessionUtils.getUserId());
    }

    @GetMapping("/api/project/stop/{moduleType}/{id}")
    @Operation(summary = "项目-任务中心-接口用例/场景-停止任务")
    public void stopById(@PathVariable String moduleType, @PathVariable String id) {
        apiTaskCenterService.hasPermission(PROJECT, moduleType,
                SessionUtils.getCurrentOrganizationId(),
                SessionUtils.getCurrentProjectId());
        apiTaskCenterService.stopById(moduleType, id, SessionUtils.getUserId(),
                OperationLogModule.PROJECT_MANAGEMENT_TASK_CENTER, null);
    }

    @GetMapping("/api/org/stop/{moduleType}/{id}")
    @Operation(summary = "组织-任务中心-接口用例/场景-停止任务")
    public void stopOrgById(@PathVariable String moduleType, @PathVariable String id) {
        apiTaskCenterService.orgStopById(moduleType, id, SessionUtils.getUserId(),
                OperationLogModule.SETTING_ORGANIZATION_TASK_CENTER);
    }

    @GetMapping("/api/system/stop/{moduleType}/{id}")
    @Operation(summary = "系统-任务中心-接口用例/场景-停止任务")
    @RequiresPermissions(PermissionConstants.PROJECT_API_REPORT_READ)
    public void stopSystemById(@PathVariable String moduleType, @PathVariable String id) {
        apiTaskCenterService.systemStopById(moduleType, id, SessionUtils.getUserId(),
                OperationLogModule.SETTING_SYSTEM_TASK_CENTER);
    }
}
