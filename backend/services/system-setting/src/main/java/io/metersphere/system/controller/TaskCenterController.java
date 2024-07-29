package io.metersphere.system.controller;

import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.taskcenter.TaskCenterScheduleDTO;
import io.metersphere.system.dto.taskcenter.request.TaskCenterScheduleBatchRequest;
import io.metersphere.system.dto.taskcenter.request.TaskCenterSchedulePageRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.service.TaskCenterService;
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
@Tag(name = "任务中心-定时任务")
public class TaskCenterController {

    @Resource
    private TaskCenterService taskCenterService;
    private static final String PROJECT = "project";

    @PostMapping("/project/schedule/page")
    @Operation(summary = "项目-任务中心-定时任务列表")
    public Pager<List<TaskCenterScheduleDTO>> projectScheduleList(@Validated @RequestBody TaskCenterSchedulePageRequest request) {
        return taskCenterService.getProjectSchedulePage(request, SessionUtils.getCurrentProjectId());
    }

    @PostMapping("/org/schedule/page")
    @Operation(summary = "组织-任务中心-定时任务列表")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TASK_CENTER_READ)
    public Pager<List<TaskCenterScheduleDTO>> orgScheduleList(@Validated @RequestBody TaskCenterSchedulePageRequest request) {
        return taskCenterService.getOrgSchedulePage(request, SessionUtils.getCurrentOrganizationId());
    }

    @PostMapping("/system/schedule/page")
    @Operation(summary = "系统-任务中心-定时任务列表")
    @RequiresPermissions(PermissionConstants.SYSTEM_TASK_CENTER_READ)
    public Pager<List<TaskCenterScheduleDTO>> systemScheduleList(@Validated @RequestBody TaskCenterSchedulePageRequest request) {
        return taskCenterService.getSystemSchedulePage(request);
    }

    @GetMapping("/system/schedule/delete/{moduleType}/{id}")
    @Operation(summary = "系统-任务中心-删除定时任务")
    public void delete(@PathVariable String moduleType, @PathVariable String id) {
        taskCenterService.checkSystemPermission(moduleType, id);
        taskCenterService.delete(id, moduleType, SessionUtils.getUserId(), "/task/center/system/schedule/delete/", OperationLogModule.SETTING_SYSTEM_TASK_CENTER);
    }

    @GetMapping("/org/schedule/delete/{moduleType}/{id}")
    @Operation(summary = "组织-任务中心-删除定时任务")
    public void deleteOrg(@PathVariable String moduleType, @PathVariable String id) {
        taskCenterService.checkOrgPermission(moduleType, id);
        taskCenterService.delete(id, moduleType, SessionUtils.getUserId(), "/task/center/org/schedule/delete/", OperationLogModule.SETTING_ORGANIZATION_TASK_CENTER);
    }

    @GetMapping("/project/schedule/delete/{moduleType}/{id}")
    @Operation(summary = "项目-任务中心-删除定时任务")
    @CheckOwner(resourceId = "#id", resourceType = "schedule")
    public void deleteProject(@PathVariable String moduleType, @PathVariable String id) {
        taskCenterService.hasPermission(PROJECT, moduleType,
                SessionUtils.getCurrentOrganizationId(), SessionUtils.getCurrentProjectId());
        taskCenterService.delete(id, moduleType, SessionUtils.getUserId(), "/task/center/project/schedule/delete/", OperationLogModule.PROJECT_MANAGEMENT_TASK_CENTER);
    }

    @GetMapping("/system/schedule/switch/{moduleType}/{id}")
    @Operation(summary = "系统-任务中心-定时任务开启关闭")
    public void enable(@PathVariable String moduleType, @PathVariable String id) {
        taskCenterService.checkSystemPermission(moduleType, id);
        taskCenterService.enable(id, moduleType, SessionUtils.getUserId(), "/task/center/system/schedule/switch/", OperationLogModule.SETTING_SYSTEM_TASK_CENTER);
    }


    @GetMapping("/org/schedule/switch/{moduleType}/{id}")
    @Operation(summary = "组织-任务中心-定时任务开启关闭")
    public void enableOrg(@PathVariable String moduleType, @PathVariable String id) {
        taskCenterService.checkOrgPermission(moduleType, id);
        taskCenterService.enable(id, moduleType, SessionUtils.getUserId(), "/task/center/org/schedule/switch/", OperationLogModule.SETTING_ORGANIZATION_TASK_CENTER);
    }

    @GetMapping("/project/schedule/switch/{moduleType}/{id}")
    @Operation(summary = "项目-任务中心-定时任务开启关闭")
    @CheckOwner(resourceId = "#id", resourceType = "schedule")
    public void enableProject(@PathVariable String moduleType, @PathVariable String id) {
        taskCenterService.hasPermission(PROJECT, moduleType,
                SessionUtils.getCurrentOrganizationId(), SessionUtils.getCurrentProjectId());
        taskCenterService.enable(id, moduleType, SessionUtils.getUserId(), "/task/center/project/schedule/switch/", OperationLogModule.PROJECT_MANAGEMENT_TASK_CENTER);
    }

    @PostMapping("/system/schedule/update/{moduleType}/{id}")
    @Operation(summary = "系统-任务中心-修改定时任务")
    public void updateSystem(@PathVariable String moduleType, @PathVariable String id, @RequestBody Object cron) {
        taskCenterService.checkSystemPermission(moduleType, id);
        taskCenterService.update(id, moduleType, cron.toString(), SessionUtils.getUserId(), "/task/center/system/schedule/update/", OperationLogModule.SETTING_SYSTEM_TASK_CENTER);
    }

    @PostMapping("/org/schedule/update/{moduleType}/{id}")
    @Operation(summary = "组织-任务中心-修改定时任务")
    public void updateOrg(@PathVariable String moduleType, @PathVariable String id, @RequestBody Object cron) {
        taskCenterService.checkOrgPermission(moduleType, id);
        taskCenterService.update(id, moduleType, cron.toString(), SessionUtils.getUserId(), "/task/center/org/schedule/update/", OperationLogModule.SETTING_ORGANIZATION_TASK_CENTER);
    }

    @PostMapping("/project/schedule/update/{moduleType}/{id}")
    @Operation(summary = "项目-任务中心-修改定时任务")
    @CheckOwner(resourceId = "#id", resourceType = "schedule")
    public void updateProject(@PathVariable String moduleType, @PathVariable String id, @RequestBody Object cron) {
        taskCenterService.hasPermission(PROJECT, moduleType,
                SessionUtils.getCurrentOrganizationId(), SessionUtils.getCurrentProjectId());
        taskCenterService.update(id, moduleType, cron.toString(), SessionUtils.getUserId(), "/task/center/project/schedule/update/", OperationLogModule.PROJECT_MANAGEMENT_TASK_CENTER);
    }

    @PostMapping("/system/schedule/batch-enable")
    @Operation(summary = "系统-任务中心-定时任务批量开启")
    public void batchEnable(@Validated @RequestBody TaskCenterScheduleBatchRequest request) {
        taskCenterService.systemBatchEnable(request, SessionUtils.getUserId(), "/task/center/system/schedule/batch-enable", OperationLogModule.SETTING_SYSTEM_TASK_CENTER, true, SessionUtils.getCurrentProjectId());
    }

    @PostMapping("/org/schedule/batch-enable")
    @Operation(summary = "组织-任务中心-定时任务批量开启")
    public void batchOrgEnable(@Validated @RequestBody TaskCenterScheduleBatchRequest request) {
        taskCenterService.orgBatchEnable(request, SessionUtils.getUserId(), SessionUtils.getCurrentOrganizationId(), "/task/center/org/schedule/batch-enable", OperationLogModule.SETTING_ORGANIZATION_TASK_CENTER, true, SessionUtils.getCurrentProjectId());
    }

    @PostMapping("/project/schedule/batch-enable")
    @Operation(summary = "项目-任务中心-定时任务批量开启")
    public void batchProjectEnable(@Validated @RequestBody TaskCenterScheduleBatchRequest request) {
        taskCenterService.hasPermission(PROJECT, request.getScheduleTagType(), SessionUtils.getCurrentOrganizationId(), SessionUtils.getCurrentProjectId());
        taskCenterService.batchEnableProject(request, SessionUtils.getUserId(), SessionUtils.getCurrentProjectId(), "/task/center/project/schedule/batch-enable", OperationLogModule.PROJECT_MANAGEMENT_TASK_CENTER, true);
    }

    @PostMapping("/system/schedule/batch-disable")
    @Operation(summary = "系统-任务中心-定时任务批量关闭")
    public void batchDisable(@Validated @RequestBody TaskCenterScheduleBatchRequest request) {
        taskCenterService.systemBatchEnable(request, SessionUtils.getUserId(), "/task/center/system/schedule/batch-disable", OperationLogModule.SETTING_SYSTEM_TASK_CENTER, false, SessionUtils.getCurrentProjectId());
    }

    @PostMapping("/org/schedule/batch-disable")
    @Operation(summary = "组织-任务中心-定时任务批量关闭")
    public void batchOrgDisable(@Validated @RequestBody TaskCenterScheduleBatchRequest request) {
        taskCenterService.orgBatchEnable(request, SessionUtils.getUserId(), SessionUtils.getCurrentOrganizationId(), "/task/center/org/schedule/batch-disable", OperationLogModule.SETTING_ORGANIZATION_TASK_CENTER, false, SessionUtils.getCurrentProjectId());
    }

    @PostMapping("/project/schedule/batch-disable")
    @Operation(summary = "项目-任务中心-定时任务批量关闭")
    public void batchProjectDisable(@Validated @RequestBody TaskCenterScheduleBatchRequest request) {
        taskCenterService.hasPermission(PROJECT, request.getScheduleTagType(),
                SessionUtils.getCurrentOrganizationId(), SessionUtils.getCurrentProjectId());
        taskCenterService.batchEnableProject(request, SessionUtils.getUserId(), SessionUtils.getCurrentProjectId(), "/task/center/project/schedule/batch-disable", OperationLogModule.PROJECT_MANAGEMENT_TASK_CENTER, false);
    }

    @GetMapping("/system/schedule/total")
    @Operation(summary = "系统-任务中心-定时任务总数")
    public int systemScheduleTotal() {
        return taskCenterService.getSystemScheduleTotal();
    }

    @GetMapping("/org/schedule/total")
    @Operation(summary = "组织-任务中心-定时任务总数")
    public int orgScheduleTotal() {
        return taskCenterService.getOrgScheduleTotal(SessionUtils.getCurrentOrganizationId());
    }

    @GetMapping("/project/schedule/total")
    @Operation(summary = "项目-任务中心-定时任务总数")
    public int projectScheduleTotal() {
        return taskCenterService.getProjectScheduleTotal(SessionUtils.getCurrentProjectId());
    }

    @GetMapping("/system/real/total")
    @Operation(summary = "系统-任务中心-实时任务总数")
    public int systemRealTotal() {
        return taskCenterService.getSystemRealTotal();
    }

    @GetMapping("/org/real/total")
    @Operation(summary = "组织-任务中心-实时任务总数")
    public int orgRealTotal() {
        return taskCenterService.getOrgRealTotal(SessionUtils.getCurrentOrganizationId());
    }

    @GetMapping("/project/real/total")
    @Operation(summary = "项目-任务中心-实时任务总数")
    public int projectRealTotal() {
        return taskCenterService.getProjectRealTotal(SessionUtils.getCurrentProjectId());
    }
}
