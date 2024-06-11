package io.metersphere.plan.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.plan.dto.request.TestPlanApiScenarioModuleRequest;
import io.metersphere.plan.dto.request.TestPlanApiScenarioRequest;
import io.metersphere.plan.dto.response.TestPlanApiScenarioPageResponse;
import io.metersphere.plan.service.TestPlanApiScenarioService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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


    @PostMapping("/page")
    @Operation(summary = "测试计划-已关联场景用例列表分页查询")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public Pager<List<TestPlanApiScenarioPageResponse>> page(@Validated @RequestBody TestPlanApiScenarioRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString("id")) ? request.getSortString("id") : "create_time desc");
        return PageUtils.setPageInfo(page, testPlanApiScenarioService.hasRelateApiScenarioList(request, false));
    }

    @PostMapping("/module/count")
    @Operation(summary = "测试计划-已关联场景用例模块数量")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public Map<String, Long> moduleCount(@Validated @RequestBody TestPlanApiScenarioModuleRequest request) {
        return testPlanApiScenarioService.moduleCount(request);
    }
}
