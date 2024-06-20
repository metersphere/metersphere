package io.metersphere.plan.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.scenario.ApiScenarioReportDTO;
import io.metersphere.api.dto.scenario.ApiScenarioReportDetailDTO;
import io.metersphere.api.service.scenario.ApiScenarioReportService;
import io.metersphere.plan.dto.request.*;
import io.metersphere.plan.dto.response.TestPlanApiScenarioPageResponse;
import io.metersphere.plan.dto.response.TestPlanAssociationResponse;
import io.metersphere.plan.dto.response.TestPlanOperationResponse;
import io.metersphere.plan.service.TestPlanApiScenarioBatchRunService;
import io.metersphere.plan.service.TestPlanApiScenarioLogService;
import io.metersphere.plan.service.TestPlanApiScenarioService;
import io.metersphere.plan.service.TestPlanService;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
import io.metersphere.system.dto.LogInsertModule;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
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
    @Resource
    private ApiScenarioReportService apiScenarioReportService;

    @PostMapping("/page")
    @Operation(summary = "测试计划-已关联场景用例列表分页查询")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public Pager<List<TestPlanApiScenarioPageResponse>> page(@Validated @RequestBody TestPlanApiScenarioRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString("id")) ? request.getSortString("id") : "test_plan_api_scenario.pos asc");
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
    public List<BaseTreeNode> getTree(@Validated @RequestBody TestPlanTreeRequest request) {
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


    @PostMapping("/batch/disassociate")
    @Operation(summary = "测试计划-计划详情-列表-批量取消关联用例")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_ASSOCIATION)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public TestPlanAssociationResponse batchDisassociate(@Validated @RequestBody BasePlanCaseBatchRequest request) {
        TestPlanAssociationResponse response = testPlanApiScenarioService.disassociate(request, new LogInsertModule(SessionUtils.getUserId(), "/test-plan/api/scenario/batch/disassociate", HttpMethodConstants.POST.name()));
        testPlanService.refreshTestPlanStatus(request.getTestPlanId());
        return response;
    }

    @PostMapping("/batch/update/executor")
    @Operation(summary = "测试计划-计划详情-场景用例列表-批量更新执行人")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.batchUpdateExecutor(#request)", msClass = TestPlanApiScenarioLogService.class)
    public void batchUpdateExecutor(@Validated @RequestBody TestPlanApiScenarioUpdateRequest request) {
        testPlanApiScenarioService.batchUpdateExecutor(request);
    }

    @GetMapping("/report/get/{id}")
    @Operation(summary = "测试计划-计划详情-场景用例列表-查看执行结果")
    @CheckOwner(resourceId = "#id", resourceType = "api_scenario_report")
    @RequiresPermissions(value = {PermissionConstants.TEST_PLAN_REPORT_READ, PermissionConstants.TEST_PLAN_READ_EXECUTE}, logical = Logical.OR)
    public ApiScenarioReportDTO get(@PathVariable String id) {
        testPlanApiScenarioService.checkReportIsTestPlan(id);
        return apiScenarioReportService.get(id);
    }

    @GetMapping("report/get/detail/{reportId}/{stepId}")
    @Operation(summary = "测试计划-计划详情-场景用例列表-执行结果详情获取")
    @CheckOwner(resourceId = "#reportId", resourceType = "api_scenario_report")
    @RequiresPermissions(value = {PermissionConstants.TEST_PLAN_REPORT_READ, PermissionConstants.TEST_PLAN_READ_EXECUTE}, logical = Logical.OR)
    public List<ApiScenarioReportDetailDTO> getDetail(@PathVariable String reportId,
                                                      @PathVariable String stepId) {
        testPlanApiScenarioService.checkReportIsTestPlan(reportId);
        return apiScenarioReportService.getDetail(reportId, stepId);
    }

    @PostMapping(value = "/sort")
    @Operation(summary = "测试计划-场景用例拖拽排序")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_UPDATE)
    public TestPlanOperationResponse sortNode(@Validated @RequestBody ResourceSortRequest request) {
        return testPlanApiScenarioService.sortNode(request, new LogInsertModule(SessionUtils.getUserId(), "/test-plan/api/scenario/sort", HttpMethodConstants.POST.name()));
    }

    @PostMapping("/batch/move")
    @Operation(summary = "测试计划-计划详情-场景用例-批量移动")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.batchMove(#request)", msClass = TestPlanApiScenarioLogService.class)
    public void batchMove(@Validated @RequestBody BaseBatchMoveRequest request) {
        testPlanApiScenarioService.batchMove(request);
    }
}
