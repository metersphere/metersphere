package io.metersphere.plan.controller;

import io.metersphere.plan.service.TestPlanTaskCenterService;
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

@RestController
@RequestMapping(value = "/task/center/plan")
@Tag(name = "任务中心-实时任务-测试计划")
public class TestPlanTaskCenterController {

    @Resource
    private TestPlanTaskCenterService testPlanTaskCenterService;

    private static final String PROJECT = "project";

    @PostMapping("/project/real-time/page")
    @Operation(summary = "项目-任务中心-测试计划-实时任务列表")
    public Pager<List<TaskCenterDTO>> projectList(@Validated @RequestBody TaskCenterPageRequest request) {
        return testPlanTaskCenterService.getProjectPage(request, SessionUtils.getCurrentProjectId());
    }

    @PostMapping("/org/real-time/page")
    @Operation(summary = "组织-任务中心-测试计划-实时任务列表")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TASK_CENTER_READ)
    public Pager<List<TaskCenterDTO>> orgList(@Validated @RequestBody TaskCenterPageRequest request) {
        return testPlanTaskCenterService.getOrganizationPage(request, SessionUtils.getCurrentOrganizationId());
    }

    @PostMapping("/system/real-time/page")
    @Operation(summary = "系统-任务中心-测试计划-实时任务列表")
    @RequiresPermissions(PermissionConstants.SYSTEM_TASK_CENTER_READ)
    public Pager<List<TaskCenterDTO>> systemList(@Validated @RequestBody TaskCenterPageRequest request) {
        return testPlanTaskCenterService.getSystemPage(request);
    }

    @GetMapping("/project/stop/{id}")
    @Operation(summary = "项目-任务中心-接口用例/场景-停止任务")
    public void stopById(@PathVariable String id) {
        testPlanTaskCenterService.hasPermission(PROJECT,
                SessionUtils.getCurrentOrganizationId(),
                SessionUtils.getCurrentProjectId());
        testPlanTaskCenterService.stopById(id, SessionUtils.getUserId(),
                OperationLogModule.PROJECT_MANAGEMENT_TASK_CENTER, null);
    }

    @GetMapping("/org/stop/{id}")
    @Operation(summary = "组织-任务中心-接口用例/场景-停止任务")
    public void stopOrgById(@PathVariable String id) {
        testPlanTaskCenterService.orgStopById(id, SessionUtils.getUserId(),
                OperationLogModule.SETTING_ORGANIZATION_TASK_CENTER);
    }

    @GetMapping("/system/stop/{id}")
    @Operation(summary = "系统-任务中心-接口用例/场景-停止任务")
    @RequiresPermissions(PermissionConstants.PROJECT_API_REPORT_READ)
    public void stopSystemById(@PathVariable String id) {
        testPlanTaskCenterService.systemStopById(id, SessionUtils.getUserId(),
                OperationLogModule.SETTING_SYSTEM_TASK_CENTER);
    }

    @PostMapping("/system/stop")
    @Operation(summary = "系统-任务中心-接口用例/场景-停止任务")
    public void systemStop(@Validated @RequestBody TaskCenterBatchRequest request) {
        testPlanTaskCenterService.systemStop(request, SessionUtils.getUserId());
    }

    @PostMapping("/org/stop")
    @Operation(summary = "组织-任务中心-接口用例/场景-停止任务")
    public void orgStop(@Validated @RequestBody TaskCenterBatchRequest request) {
        testPlanTaskCenterService.orgStop(request, SessionUtils.getCurrentOrganizationId(), SessionUtils.getUserId());
    }

    @PostMapping("/project/stop")
    @Operation(summary = "项目-任务中心-接口用例/场景-停止任务")
    public void projectStop(@Validated @RequestBody TaskCenterBatchRequest request) {
        testPlanTaskCenterService.hasPermission(PROJECT,
                SessionUtils.getCurrentOrganizationId(),
                SessionUtils.getCurrentProjectId());
        testPlanTaskCenterService.projectStop(request, SessionUtils.getCurrentProjectId(), SessionUtils.getUserId());
    }
}
