package io.metersphere.plan.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.plan.dto.request.*;
import io.metersphere.plan.dto.response.TestPlanApiScenarioPageResponse;
import io.metersphere.plan.dto.response.TestPlanAssociationResponse;
import io.metersphere.plan.service.TestPlanApiScenarioBatchRunService;
import io.metersphere.plan.service.TestPlanApiScenarioService;
import io.metersphere.plan.service.TestPlanService;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
import io.metersphere.system.dto.LogInsertModule;
import io.metersphere.system.dto.sdk.BaseTreeNode;
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
    @Resource
    private TestPlanApiScenarioBatchRunService testPlanApiScenarioBatchRunService;
    @Resource
    private TestPlanService testPlanService;

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

    @PostMapping("/tree")
    @Operation(summary = "测试计划-已关联场景用例列表模块树")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public List<BaseTreeNode> getTree(@Validated @RequestBody TestPlanApiScenarioTreeRequest request) {
        return testPlanApiScenarioService.getTree(request);
    }

    @GetMapping("/run/{id}")
    @Operation(summary = "接口测试-接口场景管理-场景执行")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_EXECUTE)
//    @CheckOwner(resourceId = "#id", resourceType = "test_plan_api_scenario")
    public TaskRequestDTO run(@PathVariable String id, @RequestParam(required = false) String reportId) {
        return testPlanApiScenarioService.run(id, reportId, SessionUtils.getUserId());
    }

    @PostMapping("/batch/run")
    @Operation(summary = "批量执行")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_EXECUTE)
//    @CheckOwner(resourceId = "#request.getId()", resourceType = "test_plan_api_case") todo
    public void batchRun(@Validated @RequestBody TestPlanApiScenarioBatchRunRequest request) {
        testPlanApiScenarioBatchRunService.asyncBatchRun(request, SessionUtils.getUserId());
    }

    @PostMapping("/disassociate")
    @Operation(summary = "测试计划-计划详情-场景用例列表-取消关联用例")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_ASSOCIATION)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public TestPlanAssociationResponse disassociate(@Validated @RequestBody TestPlanDisassociationRequest request) {
        BasePlanCaseBatchRequest batchRequest = new BasePlanCaseBatchRequest();
        batchRequest.setTestPlanId(request.getTestPlanId());
        batchRequest.setSelectIds(List.of(request.getId()));
        TestPlanAssociationResponse response = testPlanApiScenarioService.disassociate(batchRequest, new LogInsertModule(SessionUtils.getUserId(), "/test-plan/api/scenario/disassociate", HttpMethodConstants.POST.name()));
        testPlanService.refreshTestPlanStatus(request.getTestPlanId());
        return response;
    }
}
