package io.metersphere.plan.controller;

import io.metersphere.plan.constants.TestPlanResourceConfig;
import io.metersphere.plan.dto.request.TestPlanBatchExecuteRequest;
import io.metersphere.plan.dto.request.TestPlanExecuteRequest;
import io.metersphere.plan.service.TestPlanExecuteService;
import io.metersphere.plan.service.TestPlanLogService;
import io.metersphere.plan.service.TestPlanManagementService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.security.CheckOwner;
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

import java.util.Collections;

@RestController
@RequestMapping("/test-plan-execute")
@Tag(name = "测试计划执行")
public class TestPlanExecuteController {

    @Resource
    private TestPlanManagementService testPlanManagementService;

    @Resource
    private TestPlanExecuteService testPlanExecuteService;

    @PostMapping("/single")
    @Operation(summary = "测试计划单独执行")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_EXECUTE)
    @CheckOwner(resourceId = "#request.getExecuteId()", resourceType = "test_plan")
    @Log(type = OperationLogType.EXECUTE, expression = "#msClass.executeLog(#request)", msClass = TestPlanLogService.class)
    public String startExecute(@Validated @RequestBody TestPlanExecuteRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getExecuteId(), TestPlanResourceConfig.CONFIG_TEST_PLAN, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        return testPlanExecuteService.singleExecuteTestPlan(request, SessionUtils.getUserId());
    }

    @PostMapping("/batch")
    @Operation(summary = "测试计划-开始自行")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_EXECUTE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    @Log(type = OperationLogType.EXECUTE, expression = "#msClass.batchExecuteLog(#request)", msClass = TestPlanLogService.class)
    public void startExecute(@Validated @RequestBody TestPlanBatchExecuteRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getProjectId(), TestPlanResourceConfig.CHECK_TYPE_PROJECT, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        testPlanExecuteService.batchExecuteTestPlan(request, SessionUtils.getUserId());
    }

}
