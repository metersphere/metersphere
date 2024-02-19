package io.metersphere.api.controller;

import io.metersphere.api.service.ApiTaskCenterService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.taskcenter.TaskCenterDTO;
import io.metersphere.system.dto.taskcenter.request.TaskCenterBatchRequest;
import io.metersphere.system.dto.taskcenter.request.TaskCenterPageRequest;
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


    @PostMapping("/api/project/real-time/page")
    @Operation(summary = "项目-任务中心-接口用例/场景-实时任务列表")
    @RequiresPermissions(PermissionConstants.PROJECT_API_REPORT_READ)
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
    @RequiresPermissions(PermissionConstants.SYSTEM_TASK_CENTER_READ)
    public void systemStop(@Validated @RequestBody TaskCenterBatchRequest request) {
        apiTaskCenterService.systemStop(request);
    }

    @PostMapping("/api/org/stop")
    @Operation(summary = "组织-任务中心-接口用例/场景-停止任务")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TASK_CENTER_READ_STOP)
    public void orgStop(@Validated @RequestBody TaskCenterBatchRequest request) {
        apiTaskCenterService.orgStop(request, SessionUtils.getCurrentOrganizationId());
    }

    @PostMapping("/api/project/stop")
    @Operation(summary = "项目-任务中心-接口用例/场景-停止任务")
    @RequiresPermissions(PermissionConstants.PROJECT_API_REPORT_READ)
    public void projectStop(@Validated @RequestBody TaskCenterBatchRequest request) {
        apiTaskCenterService.projectStop(request, SessionUtils.getCurrentProjectId());
    }

    @GetMapping("/api/project/stop/{id}")
    @Operation(summary = "任务中心-接口用例/场景-停止任务")
    public void stopById(@PathVariable String id) {
        apiTaskCenterService.stopById(id);
    }


}
