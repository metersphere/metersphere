package io.metersphere.project.controller;

import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.sdk.BasePageRequest;
import io.metersphere.system.dto.taskhub.ResourcePoolOptionsDTO;
import io.metersphere.system.dto.taskhub.TaskHubDTO;
import io.metersphere.system.dto.taskhub.TaskHubItemDTO;
import io.metersphere.system.dto.taskhub.TaskHubScheduleDTO;
import io.metersphere.system.dto.taskhub.request.TaskHubItemRequest;
import io.metersphere.system.dto.taskhub.response.TaskStatisticsResponse;
import io.metersphere.system.service.BaseTaskHubService;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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


    @PostMapping("/exec-task/item/page")
    @Operation(summary = "项目-任务中心-用例执行任务详情列表")
    @RequiresPermissions(PermissionConstants.PROJECT_CASE_TASK_CENTER_READ)
    public Pager<List<TaskHubItemDTO>> itemPageList(@Validated @RequestBody TaskHubItemRequest request) {
        return baseTaskHubService.getCaseTaskItemList(request, null, SessionUtils.getCurrentProjectId());
    }


    @PostMapping("/exec-task/statistics")
    @Operation(summary = "项目-任务中心-获取任务统计{通过率}接口")
    @RequiresPermissions(PermissionConstants.PROJECT_CASE_TASK_CENTER_READ)
    @Parameter(name = "ids", description = "任务ID集合", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    public List<TaskStatisticsResponse> calculateRate(@RequestBody List<String> ids) {
        return baseTaskHubService.calculateRate(ids, null, SessionUtils.getCurrentProjectId());
    }


    @GetMapping("/resource-pool/options")
    @Operation(summary = "项目-任务中心-获取资源池下拉选项")
    @RequiresPermissions(PermissionConstants.PROJECT_CASE_TASK_CENTER_READ)
    public List<ResourcePoolOptionsDTO> getUserProject() {
        return baseTaskHubService.getProjectResourcePoolOptions(SessionUtils.getCurrentProjectId());
    }
}
