package io.metersphere.system.controller;

import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.taskcenter.TaskCenterScheduleDTO;
import io.metersphere.system.dto.taskcenter.request.TaskCenterSchedulePageRequest;
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


    @PostMapping("/project/schedule/page")
    @Operation(summary = "项目-任务中心-定时任务列表")
    @RequiresPermissions(PermissionConstants.PROJECT_BASE_INFO_READ)
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

    @GetMapping("/schedule/delete/{id}")
    @Operation(summary = "系统-任务中心-删除定时任务")
    @CheckOwner(resourceId = "#id", resourceType = "schedule")
    public void delete(@PathVariable String id) {
        taskCenterService.delete(id);
    }

    @GetMapping("/schedule/switch/{id}")
    @Operation(summary = "系统-任务中心-关闭/开启定时任务")
    @CheckOwner(resourceId = "#id", resourceType = "schedule")
    public void enable(@PathVariable String id) {
        taskCenterService.enable(id);
    }

    @PostMapping("/schedule/update/{id}/{cron}")
    @Operation(summary = "系统-任务中心-修改定时任务")
    @CheckOwner(resourceId = "#id", resourceType = "schedule")
    public void update(@PathVariable String id, @PathVariable String cron) {
        taskCenterService.update(id, cron);
    }



}
