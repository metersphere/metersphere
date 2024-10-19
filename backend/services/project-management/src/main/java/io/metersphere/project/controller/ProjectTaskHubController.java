package io.metersphere.project.controller;

import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.sdk.BasePageRequest;
import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.metersphere.system.dto.taskhub.ResourcePoolOptionsDTO;
import io.metersphere.system.dto.taskhub.TaskHubDTO;
import io.metersphere.system.dto.taskhub.TaskHubItemDTO;
import io.metersphere.system.dto.taskhub.TaskHubScheduleDTO;
import io.metersphere.system.dto.taskhub.request.TaskHubItemBatchRequest;
import io.metersphere.system.dto.taskhub.request.TaskHubItemRequest;
import io.metersphere.system.dto.taskhub.response.TaskStatisticsResponse;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.service.BaseTaskHubLogService;
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
import java.util.Map;

@Tag(name = "项目任务中心")
@RestController
@RequestMapping("/project/task-center")
public class ProjectTaskHubController {

    @Resource
    private BaseTaskHubService baseTaskHubService;
    @Resource
    private BaseTaskHubLogService baseTaskHubLogService;

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


    @GetMapping("/exec-task/stop/{id}")
    @Operation(summary = "项目-任务中心-用例执行任务-停止任务")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.projectStopLog(#id)", msClass = BaseTaskHubLogService.class)
    @RequiresPermissions(PermissionConstants.PROJECT_CASE_TASK_CENTER_EXEC_STOP)
    public void stopTask(@PathVariable String id) {
        baseTaskHubService.stopTask(id, SessionUtils.getUserId(), null, SessionUtils.getCurrentProjectId());
    }


    @PostMapping("/exec-task/batch-stop")
    @Operation(summary = "项目-任务中心-用例执行任务-批量停止任务")
    @RequiresPermissions(PermissionConstants.PROJECT_CASE_TASK_CENTER_EXEC_STOP)
    public void batchStopTask(@Validated @RequestBody TableBatchProcessDTO request) {
        List<String> ids = baseTaskHubService.getTaskIds(request, null, SessionUtils.getCurrentProjectId());
        baseTaskHubService.batchStopTask(ids, SessionUtils.getUserId(), null, SessionUtils.getCurrentProjectId());
        baseTaskHubLogService.taskBatchLog(ids, SessionUtils.getUserId(), OperationLogType.STOP.name(), SessionUtils.getCurrentProjectId(), SessionUtils.getCurrentOrganizationId(),
                "/project/task-center/exec-task/batch-stop", OperationLogModule.PROJECT_MANAGEMENT_TASK_CENTER);
    }

    @PostMapping("/exec-task/item/order")
    @Operation(summary = "系统-任务中心-用例执行任务-获取任务项的排队信息")
    @RequiresPermissions(PermissionConstants.PROJECT_CASE_TASK_CENTER_READ)
    public Map<String, Integer> getTaskItemOrder(@RequestBody List<String> taskIdItemIds) {
        return baseTaskHubService.getTaskItemOrder(taskIdItemIds);
    }

    @GetMapping("/exec-task/delete/{id}")
    @Operation(summary = "项目-任务中心-用例执行任务-删除任务")
    @Log(type = OperationLogType.DELETE, expression = "#msClass.projectDeleteLog(#id)", msClass = BaseTaskHubLogService.class)
    @RequiresPermissions(PermissionConstants.PROJECT_CASE_TASK_CENTER_DELETE)
    public void deleteTask(@PathVariable String id) {
        baseTaskHubService.deleteTask(id, null, SessionUtils.getCurrentProjectId());
    }

    @PostMapping("/exec-task/batch-delete")
    @Operation(summary = "项目-任务中心-用例执行任务-批量删除任务")
    @RequiresPermissions(PermissionConstants.PROJECT_CASE_TASK_CENTER_DELETE)
    public void batchDeleteTask(@Validated @RequestBody TableBatchProcessDTO request) {
        List<String> ids = baseTaskHubService.getTaskIds(request, null, SessionUtils.getCurrentProjectId());
        baseTaskHubService.batchDeleteTask(ids, null, SessionUtils.getCurrentProjectId());
        baseTaskHubLogService.taskBatchLog(ids, SessionUtils.getUserId(), OperationLogType.DELETE.name(), SessionUtils.getCurrentProjectId(), SessionUtils.getCurrentOrganizationId(),
                "/project/task-center/exec-task/batch-delete", OperationLogModule.PROJECT_MANAGEMENT_TASK_CENTER);
    }


    @GetMapping("/exec-task/item/stop/{id}")
    @Operation(summary = "项目-任务中心-用例任务详情-停止任务")
    @Log(type = OperationLogType.STOP, expression = "#msClass.projectStopItemLog(#id)", msClass = BaseTaskHubLogService.class)
    @RequiresPermissions(PermissionConstants.PROJECT_CASE_TASK_CENTER_EXEC_STOP)
    public void stopTaskItem(@PathVariable String id) {
        baseTaskHubService.stopTaskItem(id, SessionUtils.getUserId(), null, null);
    }

    @PostMapping("/exec-task/item/batch-stop")
    @Operation(summary = "项目-任务中心-用例任务详情-批量停止任务")
    @RequiresPermissions(PermissionConstants.PROJECT_CASE_TASK_CENTER_EXEC_STOP)
    public void batchStopTaskItem(@Validated @RequestBody TaskHubItemBatchRequest request) {
        List<String> itemIds = baseTaskHubService.getTaskItemIds(request, null, null);
        baseTaskHubService.batchStopTaskItem(itemIds, SessionUtils.getUserId(), null, null);
        baseTaskHubLogService.taskItemBatchLog(itemIds, SessionUtils.getUserId(), OperationLogType.STOP.name(), SessionUtils.getCurrentProjectId(), SessionUtils.getCurrentOrganizationId(),
                "/project/task-center/exec-task/item/batch-stop", OperationLogModule.PROJECT_MANAGEMENT_TASK_CENTER);

    }

    @GetMapping("/schedule/delete/{id}")
    @Operation(summary = "项目-任务中心-系统后台任务-删除")
    @RequiresPermissions(PermissionConstants.PROJECT_SCHEDULE_TASK_CENTER_READ_DELETE)
    public void deleteScheduleTask(@PathVariable String id) {
        baseTaskHubService.deleteScheduleTask(id, SessionUtils.getUserId(), "/project/task-center/schedule/delete/", OperationLogModule.PROJECT_MANAGEMENT_TASK_CENTER);
    }
}
