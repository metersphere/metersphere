package io.metersphere.system.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.page.PageMethod;
import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.BatchExecTaskReportDTO;
import io.metersphere.system.dto.OrganizationProjectOptionsDTO;
import io.metersphere.system.dto.request.BatchExecTaskPageRequest;
import io.metersphere.system.dto.sdk.BasePageRequest;
import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.metersphere.system.dto.taskhub.*;
import io.metersphere.system.dto.taskhub.request.ScheduleRequest;
import io.metersphere.system.dto.taskhub.request.TaskHubItemBatchRequest;
import io.metersphere.system.dto.taskhub.request.TaskHubItemRequest;
import io.metersphere.system.dto.taskhub.response.TaskStatisticsResponse;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.service.BaseTaskHubLogService;
import io.metersphere.system.service.BaseTaskHubService;
import io.metersphere.system.service.OrganizationService;
import io.metersphere.system.service.SystemProjectService;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "系统任务中心")
@RestController
@RequestMapping("/system/task-center")
public class SystemTaskHubController {

    @Resource
    private BaseTaskHubService baseTaskHubService;
    @Resource
    private BaseTaskHubLogService baseTaskHubLogService;
    @Resource
    private SystemProjectService systemProjectService;
    @Resource
    private OrganizationService organizationService;

    @PostMapping("/exec-task/page")
    @Operation(summary = "系统-任务中心-执行任务列表")
    @RequiresPermissions(PermissionConstants.SYSTEM_CASE_TASK_CENTER_READ)
    public Pager<List<TaskHubDTO>> execTaskList(@Validated @RequestBody BasePageRequest request) {
        return baseTaskHubService.getTaskList(request, null, null);
    }


    @PostMapping("/schedule/page")
    @Operation(summary = "系统-任务中心-后台执行任务列表")
    @RequiresPermissions(PermissionConstants.SYSTEM_SCHEDULE_TASK_CENTER_READ)
    public Pager<List<TaskHubScheduleDTO>> scheduleList(@Validated @RequestBody BasePageRequest request) {
        return baseTaskHubService.getScheduleTaskList(request, null);
    }

    @PostMapping("/exec-task/item/page")
    @Operation(summary = "系统-任务中心-用例执行任务详情列表")
    @RequiresPermissions(PermissionConstants.SYSTEM_CASE_TASK_CENTER_READ)
    public Pager<List<TaskHubItemDTO>> itemPageList(@Validated @RequestBody TaskHubItemRequest request) {
        return baseTaskHubService.getCaseTaskItemList(request, null, null);
    }


    @PostMapping("/exec-task/statistics")
    @Operation(summary = "系统-任务中心-获取任务统计{通过率}接口")
    @RequiresPermissions(PermissionConstants.SYSTEM_CASE_TASK_CENTER_READ)
    @Parameter(name = "ids", description = "任务ID集合", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    public List<TaskStatisticsResponse> calculateRate(@RequestBody List<String> ids) {
        return baseTaskHubService.calculateRate(ids, null, null);
    }


    @GetMapping("/resource-pool/options")
    @Operation(summary = "系统-任务中心-获取资源池下拉选项")
    @RequiresPermissions(PermissionConstants.SYSTEM_CASE_TASK_CENTER_READ)
    public List<ResourcePoolOptionsDTO> getUserProject() {
        return baseTaskHubService.getResourcePoolOptions();
    }

    @PostMapping("/resource-pool/status")
    @Operation(summary = "任务详情节点状态接口")
    @RequiresPermissions(value = {PermissionConstants.SYSTEM_CASE_TASK_CENTER_READ, PermissionConstants.ORGANIZATION_CASE_TASK_CENTER_READ, PermissionConstants.PROJECT_CASE_TASK_CENTER_READ}, logical = Logical.OR)
    @Parameter(name = "ids", description = "详情ID集合", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    public List<ResourcePoolStatusDTO> resourcePoolStatus(@RequestBody List<String> ids) {
        return baseTaskHubService.getResourcePoolStatus(ids);
    }


    @GetMapping("/exec-task/stop/{id}")
    @Operation(summary = "系统-任务中心-用例执行任务-停止任务")
    @Log(type = OperationLogType.STOP, expression = "#msClass.systemStopLog(#id)", msClass = BaseTaskHubLogService.class)
    @RequiresPermissions(PermissionConstants.SYSTEM_CASE_TASK_CENTER_EXEC_STOP)
    public void stopTask(@PathVariable String id) {
        baseTaskHubService.stopTask(id, SessionUtils.getUserId(), null, null);
    }

    @GetMapping("/exec-task/rerun/{id}")
    @Operation(summary = "系统-任务中心-用例执行任务-重跑任务")
    @Log(type = OperationLogType.RERUN, expression = "#msClass.systemRerunLog(#id)", msClass = BaseTaskHubLogService.class)
    @RequiresPermissions(PermissionConstants.SYSTEM_CASE_TASK_CENTER_EXEC_STOP)
    public void rerunTask(@PathVariable String id) {
        baseTaskHubService.rerunTask(id, SessionUtils.getUserId(), null, null);
    }

    @PostMapping("/exec-task/batch-stop")
    @Operation(summary = "系统-任务中心-用例执行任务-批量停止任务")
    @RequiresPermissions(PermissionConstants.SYSTEM_CASE_TASK_CENTER_EXEC_STOP)
    public void batchStopTask(@Validated @RequestBody TableBatchProcessDTO request) {
        List<String> ids = baseTaskHubService.getTaskIds(request, null, null, true);
        baseTaskHubService.batchStopTask(ids, SessionUtils.getUserId(), null, null);
        //系統日志
        baseTaskHubLogService.taskBatchLog(ids, SessionUtils.getUserId(), OperationLogType.STOP.name(), OperationLogConstants.SYSTEM, OperationLogConstants.SYSTEM,
                "/system/task-center/exec-task/batch-stop", OperationLogModule.SETTING_SYSTEM_TASK_CENTER);
    }

    @PostMapping("/exec-task/item/order")
    @Operation(summary = "系统-任务中心-用例执行任务-获取任务项的排队信息")
    @RequiresPermissions(PermissionConstants.SYSTEM_CASE_TASK_CENTER_READ)
    public Map<String, Integer> getTaskItemOrder(@RequestBody List<String> taskIdItemIds) {
        return baseTaskHubService.getTaskItemOrder(taskIdItemIds);
    }

    @GetMapping("/exec-task/delete/{id}")
    @Operation(summary = "系统-任务中心-用例执行任务-删除任务")
    @Log(type = OperationLogType.DELETE, expression = "#msClass.systemDeleteLog(#id)", msClass = BaseTaskHubLogService.class)
    @RequiresPermissions(PermissionConstants.SYSTEM_CASE_TASK_CENTER_DELETE)
    public void deleteTask(@PathVariable String id) {
        baseTaskHubService.deleteTask(id, null, null);
    }


    @PostMapping("/exec-task/batch-delete")
    @Operation(summary = "系统-任务中心-用例执行任务-批量删除任务")
    @RequiresPermissions(PermissionConstants.SYSTEM_CASE_TASK_CENTER_DELETE)
    public void batchDeleteTask(@Validated @RequestBody TableBatchProcessDTO request) {
        List<String> ids = baseTaskHubService.getTaskIds(request, null, null, false);
        //系統日志
        baseTaskHubLogService.taskBatchLog(ids, SessionUtils.getUserId(), OperationLogType.DELETE.name(), OperationLogConstants.SYSTEM, OperationLogConstants.SYSTEM,
                "/system/task-center/exec-task/batch-delete", OperationLogModule.SETTING_SYSTEM_TASK_CENTER);
        baseTaskHubService.batchDeleteTask(ids, SessionUtils.getCurrentOrganizationId(), null);
    }

    @PostMapping("/exec-task/batch/page")
    @Operation(summary = "组织-任务中心-用例执行任务-批量任务列表")
    @RequiresPermissions(PermissionConstants.SYSTEM_CASE_TASK_CENTER_READ)
    public Pager<List<BatchExecTaskReportDTO>> batchTaskList(@Validated @RequestBody BatchExecTaskPageRequest request) {
        String sort = StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "r.start_time desc";
        if (StringUtils.contains(request.getSortString(), "create_time")) {
            sort = sort.replace("create_time", "r.start_time");
        }
        Page<Object> page = PageMethod.startPage(request.getCurrent(), request.getPageSize(), sort);
        return PageUtils.setPageInfo(page, baseTaskHubService.listBatchTaskReport(request));
    }

    //TODO 系统&组织&项目 任务按钮操作：失败重跑 查看报告 批量失败重跑


    @GetMapping("/exec-task/item/stop/{id}")
    @Operation(summary = "系统-任务中心-用例任务详情-停止任务")
    @RequiresPermissions(PermissionConstants.SYSTEM_CASE_TASK_CENTER_EXEC_STOP)
    public void stopTaskItem(@PathVariable String id) {
        baseTaskHubService.stopTaskItem(id, SessionUtils.getUserId(), null, null);
        baseTaskHubLogService.taskItemBatchLog(List.of(id), SessionUtils.getUserId(), OperationLogType.STOP.name(), OperationLogConstants.SYSTEM, OperationLogConstants.SYSTEM,
                "/system/task-center/exec-task/item/stop/", OperationLogModule.SETTING_SYSTEM_TASK_CENTER);
    }

    @PostMapping("/exec-task/item/batch-stop")
    @Operation(summary = "系统-任务中心-用例任务详情-批量停止任务")
    @RequiresPermissions(PermissionConstants.SYSTEM_CASE_TASK_CENTER_EXEC_STOP)
    public void batchStopTaskItem(@Validated @RequestBody TaskHubItemBatchRequest request) {
        List<String> itemIds = baseTaskHubService.getTaskItemIds(request, null, null);
        baseTaskHubService.batchStopTaskItem(itemIds, SessionUtils.getUserId(), null, null);
        baseTaskHubLogService.taskItemBatchLog(itemIds, SessionUtils.getUserId(), OperationLogType.STOP.name(), OperationLogConstants.SYSTEM, OperationLogConstants.SYSTEM,
                "/system/task-center/exec-task/item/batch-stop", OperationLogModule.SETTING_SYSTEM_TASK_CENTER);

    }
    //TODO 系统&组织&项目 任务详情按钮操作：查看

    @GetMapping("/schedule/delete/{id}")
    @Operation(summary = "系统-任务中心-系统后台任务-删除")
    @RequiresPermissions(PermissionConstants.SYSTEM_SCHEDULE_TASK_CENTER_READ_DELETE)
    public void deleteScheduleTask(@PathVariable String id) {
        baseTaskHubService.deleteScheduleTask(id, SessionUtils.getUserId(), "/system/task-center/schedule/delete/", OperationLogModule.SETTING_SYSTEM_TASK_CENTER);
    }

    @GetMapping("/schedule/switch/{id}")
    @Operation(summary = "系统-任务中心-后台任务开启关闭")
    @RequiresPermissions(PermissionConstants.SYSTEM_SCHEDULE_TASK_CENTER_READ_UPDATE)
    public void enable(@PathVariable String id) {
        baseTaskHubService.enable(id, SessionUtils.getUserId(), "/system/task-center/schedule/switch/", OperationLogModule.SETTING_SYSTEM_TASK_CENTER);
    }


    @PostMapping("/schedule/batch-enable")
    @Operation(summary = "系统-任务中心-后台任务-批量开启")
    @RequiresPermissions(PermissionConstants.SYSTEM_SCHEDULE_TASK_CENTER_READ_UPDATE)
    public void batchEnable(@Validated @RequestBody TableBatchProcessDTO request) {
        baseTaskHubService.scheduleBatchOperation(request, SessionUtils.getUserId(), null, "/system/task-center/schedule/batch-enable", OperationLogModule.SETTING_SYSTEM_TASK_CENTER, true, null);
    }

    @PostMapping("/schedule/batch-disable")
    @Operation(summary = "系统-任务中心-后台任务-批量关闭")
    @RequiresPermissions(PermissionConstants.SYSTEM_SCHEDULE_TASK_CENTER_READ_UPDATE)
    public void batchDisable(@Validated @RequestBody TableBatchProcessDTO request) {
        baseTaskHubService.scheduleBatchOperation(request, SessionUtils.getUserId(), null, "/system/task-center/schedule/batch-disable", OperationLogModule.SETTING_SYSTEM_TASK_CENTER, false, null);
    }


    @PostMapping("/schedule/update-cron")
    @Operation(summary = "系统-任务中心-后台任务更新cron表达式")
    @RequiresPermissions(PermissionConstants.SYSTEM_SCHEDULE_TASK_CENTER_READ_UPDATE)
    public void updateValue(@Validated @RequestBody ScheduleRequest request) {
        baseTaskHubService.updateCron(request, SessionUtils.getUserId(), "/system/task-center/schedule/update-cron", OperationLogModule.SETTING_SYSTEM_TASK_CENTER);
    }


    @GetMapping("/project/options")
    @Operation(summary = "系统-任务中心-获取全部项目下拉选项")
    @RequiresPermissions(PermissionConstants.SYSTEM_CASE_TASK_CENTER_READ)
    public List<OrganizationProjectOptionsDTO> getAllProject() {
        List<OrganizationProjectOptionsDTO> projectList = systemProjectService.getProjectOptions(null);
        return projectList;
    }

    @GetMapping("/organization/options")
    @Operation(summary = "系统-任务中心-获取全部组织下拉选项")
    @RequiresPermissions(PermissionConstants.SYSTEM_CASE_TASK_CENTER_READ)
    public List<OrganizationProjectOptionsDTO> getAllOrganization() {
        List<OrganizationProjectOptionsDTO> organizationList = organizationService.getOrganizationOptions();
        return organizationList;
    }

}
