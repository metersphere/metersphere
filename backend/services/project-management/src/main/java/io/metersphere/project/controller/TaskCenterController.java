package io.metersphere.project.controller;

import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.taskcenter.TaskCenterScheduleDTO;
import io.metersphere.system.dto.taskcenter.request.OrganizationTaskCenterSchedulePageRequest;
import io.metersphere.system.dto.taskcenter.request.ProjectTaskCenterSchedulePageRequest;
import io.metersphere.system.dto.taskcenter.request.TaskCenterSchedulePageRequest;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.project.service.TaskCenterService;
import io.metersphere.system.utils.Pager;
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


    @PostMapping("/project/scheduled/page")
    @Operation(summary = "项目-任务中心-定时任务列表")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<TaskCenterScheduleDTO>> list(@Validated @RequestBody ProjectTaskCenterSchedulePageRequest request) {
        return taskCenterService.getProjectScheduledPage(request);
    }

    @PostMapping("/org/scheduled/page")
    @Operation(summary = "组织-任务中心-定时任务列表")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TASK_CENTER_READ)
    @CheckOwner(resourceId = "#request.getOrganizationId()", resourceType = "organization")
    public Pager<List<TaskCenterScheduleDTO>> list(@Validated @RequestBody OrganizationTaskCenterSchedulePageRequest request) {
        return taskCenterService.getOrgScheduledPage(request);
    }

    @PostMapping("/system/scheduled/page")
    @Operation(summary = "系统-任务中心-定时任务列表")
    @RequiresPermissions(PermissionConstants.SYSTEM_TASK_CENTER_READ)
    public Pager<List<TaskCenterScheduleDTO>> list(@Validated @RequestBody TaskCenterSchedulePageRequest request) {
        return taskCenterService.getSystemScheduledPage(request);
    }

    @GetMapping("/scheduled/delete/{id}")
    @Operation(summary = "系统-任务中心-删除定时任务")
    @CheckOwner(resourceId = "#id", resourceType = "scheduled")
    public void delete(@PathVariable String id) {
        taskCenterService.delete(id);
    }


}
