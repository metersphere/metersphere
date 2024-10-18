package io.metersphere.system.controller;

import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.sdk.BasePageRequest;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.metersphere.system.dto.taskhub.ResourcePoolOptionsDTO;
import io.metersphere.system.dto.taskhub.TaskHubDTO;
import io.metersphere.system.dto.taskhub.TaskHubItemDTO;
import io.metersphere.system.dto.taskhub.TaskHubScheduleDTO;
import io.metersphere.system.dto.taskhub.request.TaskHubItemRequest;
import io.metersphere.system.dto.taskhub.response.TaskStatisticsResponse;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.BaseProjectMapper;
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

@Tag(name = "组织任务中心")
@RestController
@RequestMapping("/organization/task-center")
public class OrganizationTaskHubController {

    @Resource
    private BaseTaskHubService baseTaskHubService;
    @Resource
    BaseProjectMapper baseProjectMapper;
    @Resource
    private BaseTaskHubLogService baseTaskHubLogService;

    @PostMapping("/exec-task/page")
    @Operation(summary = "组织-任务中心-执行任务列表")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_CASE_TASK_CENTER_READ)
    public Pager<List<TaskHubDTO>> projectList(@Validated @RequestBody BasePageRequest request) {
        return baseTaskHubService.getTaskList(request, SessionUtils.getCurrentOrganizationId(), null);
    }


    @PostMapping("/schedule/page")
    @Operation(summary = "组织-任务中心-后台执行任务列表")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_SCHEDULE_TASK_CENTER_READ)
    public Pager<List<TaskHubScheduleDTO>> scheduleList(@Validated @RequestBody BasePageRequest request) {
        List<OptionDTO> projectList = baseProjectMapper.getProjectOptionsByOrgId(SessionUtils.getCurrentOrganizationId());
        List<String> projectIds = projectList.stream().map(OptionDTO::getId).toList();
        return baseTaskHubService.getScheduleTaskList(request, projectIds);
    }


    @PostMapping("/exec-task/item/page")
    @Operation(summary = "组织-任务中心-用例执行任务详情列表")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_CASE_TASK_CENTER_READ)
    public Pager<List<TaskHubItemDTO>> itemPageList(@Validated @RequestBody TaskHubItemRequest request) {
        return baseTaskHubService.getCaseTaskItemList(request, SessionUtils.getCurrentOrganizationId(), null);
    }


    @PostMapping("/exec-task/statistics")
    @Operation(summary = "组织-任务中心-获取任务统计{通过率}接口")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_CASE_TASK_CENTER_READ)
    @Parameter(name = "ids", description = "任务ID集合", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    public List<TaskStatisticsResponse> calculateRate(@RequestBody List<String> ids) {
        return baseTaskHubService.calculateRate(ids, SessionUtils.getCurrentOrganizationId(), null);
    }


    @GetMapping("/resource-pool/options")
    @Operation(summary = "组织-任务中心-获取资源池下拉选项")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_CASE_TASK_CENTER_READ)
    public List<ResourcePoolOptionsDTO> getUserProject() {
        return baseTaskHubService.getOrgResourcePoolOptions(SessionUtils.getCurrentOrganizationId());
    }


    @GetMapping("/exec-task/stop/{id}")
    @Operation(summary = "组织-任务中心-用例执行任务-停止任务")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.orgStopLog(#id)", msClass = BaseTaskHubLogService.class)
    @RequiresPermissions(PermissionConstants.ORGANIZATION_CASE_TASK_CENTER_EXEC_STOP)
    public void stopTask(@PathVariable String id) {
        baseTaskHubService.stopTask(id, SessionUtils.getUserId(), SessionUtils.getCurrentOrganizationId(), null);
    }


    @PostMapping("/exec-task/batch-stop")
    @Operation(summary = "组织-任务中心-用例执行任务-批量停止任务")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_CASE_TASK_CENTER_EXEC_STOP)
    public void batchStopTask(@Validated @RequestBody TableBatchProcessDTO request) {
        List<String> ids = baseTaskHubService.getTaskIds(request, SessionUtils.getCurrentOrganizationId(), null);
        baseTaskHubService.batchStopTask(ids, SessionUtils.getUserId(), SessionUtils.getCurrentOrganizationId(), null);
        //日志
        baseTaskHubLogService.orgBatchStopLog(ids);
    }



    @GetMapping("/exec-task/delete/{id}")
    @Operation(summary = "组织-任务中心-用例执行任务-删除任务")
    @Log(type = OperationLogType.DELETE, expression = "#msClass.orgDeleteLog(#id)", msClass = BaseTaskHubLogService.class)
    @RequiresPermissions(PermissionConstants.ORGANIZATION_CASE_TASK_CENTER_DELETE)
    public void deleteTask(@PathVariable String id) {
        baseTaskHubService.deleteTask(id, SessionUtils.getCurrentOrganizationId(), null);
    }


    @PostMapping("/exec-task/batch-delete")
    @Operation(summary = "组织-任务中心-用例执行任务-批量删除任务")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_CASE_TASK_CENTER_DELETE)
    public void batchDeleteTask(@Validated @RequestBody TableBatchProcessDTO request) {
        List<String> ids = baseTaskHubService.getTaskIds(request, SessionUtils.getCurrentOrganizationId(), null);
        baseTaskHubService.batchDeleteTask(ids, SessionUtils.getCurrentOrganizationId(), null);
        //日志
        baseTaskHubLogService.orgBatchDeleteLog(ids);
    }
}
