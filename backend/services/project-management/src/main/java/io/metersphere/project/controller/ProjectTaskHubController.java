package io.metersphere.project.controller;

import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.sdk.BasePageRequest;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.taskhub.TaskHubDTO;
import io.metersphere.system.dto.taskhub.TaskHubScheduleDTO;
import io.metersphere.system.service.BaseTaskHubService;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
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

@Tag(name = "项目任务中心")
@RestController
@RequestMapping("/project/task-center")
public class ProjectTaskHubController {

    @Resource
    private BaseTaskHubService baseTaskHubService;

    @PostMapping("/exec-task/page")
    @Operation(summary = "项目-任务中心-执行任务列表")
    @RequiresPermissions(PermissionConstants.PROJECT_CASE_TASK_CENTER_READ)
    public Pager<List<TaskHubDTO>> projectList(@Validated @RequestBody BasePageRequest request) {
        return baseTaskHubService.getTaskList(request, null, SessionUtils.getCurrentProjectId());
    }

    @PostMapping("/schedule/page")
    @Operation(summary = "项目-任务中心-后台执行任务列表")
    @RequiresPermissions(PermissionConstants.PROJECT_SCHEDULE_TASK_CENTER_READ)
    public Pager<List<TaskHubScheduleDTO>> scheduleList(@Validated @RequestBody BasePageRequest request) {
        return baseTaskHubService.getScheduleTaskList(request, List.of(SessionUtils.getCurrentProjectId()));
    }
}
