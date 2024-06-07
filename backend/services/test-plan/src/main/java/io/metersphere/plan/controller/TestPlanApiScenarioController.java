package io.metersphere.plan.controller;

import io.metersphere.plan.service.TestPlanApiScenarioService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

@Tag(name = "测试计划场景用例")
@RestController
@RequestMapping("/test-plan/api/scenario")
public class TestPlanApiScenarioController {

    @Resource
    private TestPlanApiScenarioService testPlanApiScenarioService;

    @GetMapping("/run/{id}")
    @Operation(summary = "接口测试-接口场景管理-场景执行")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_EXECUTE)
//    @CheckOwner(resourceId = "#id", resourceType = "test_plan_api_scenario")
    public TaskRequestDTO run(@PathVariable String id, @RequestParam(required = false) String reportId) {
        return testPlanApiScenarioService.run(id, reportId, SessionUtils.getUserId());
    }
}
