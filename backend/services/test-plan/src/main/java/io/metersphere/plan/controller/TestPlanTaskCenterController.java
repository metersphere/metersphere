package io.metersphere.plan.controller;

import io.metersphere.plan.service.TestPlanTaskCenterService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.taskcenter.TaskCenterDTO;
import io.metersphere.system.dto.taskcenter.request.TaskCenterPageRequest;
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

@RestController
@RequestMapping(value = "/task/center/plan")
@Tag(name = "任务中心-实时任务-测试计划")
public class TestPlanTaskCenterController {

    @Resource
    private TestPlanTaskCenterService testPlanTaskCenterService;

    private static final String PROJECT = "project";
    private static final String ORG = "org";
    private static final String SYSTEM = "system";


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


}
