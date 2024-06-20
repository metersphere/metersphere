package io.metersphere.plan.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.definition.ApiReportDTO;
import io.metersphere.api.dto.definition.ApiReportDetailDTO;
import io.metersphere.api.service.definition.ApiReportService;
import io.metersphere.plan.dto.request.*;
import io.metersphere.plan.dto.response.TestPlanApiCasePageResponse;
import io.metersphere.plan.dto.response.TestPlanAssociationResponse;
import io.metersphere.plan.dto.response.TestPlanOperationResponse;
import io.metersphere.plan.service.*;
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
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "测试计划接口用例")
@RestController
@RequestMapping("/test-plan/api/case")
public class TestPlanApiCaseController {

    @Resource
    private TestPlanApiCaseService testPlanApiCaseService;
    @Resource
    private TestPlanApiCaseBatchRunService testPlanApiCaseBatchRunService;
    @Resource
    private TestPlanManagementService testPlanManagementService;
    @Resource
    private TestPlanService testPlanService;
    @Resource
    private ApiReportService apiReportService;

    @PostMapping(value = "/sort")
    @Operation(summary = "测试计划功能用例-功能用例拖拽排序")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public TestPlanOperationResponse sortNode(@Validated @RequestBody ResourceSortRequest request) {
        return testPlanApiCaseService.sortNode(request, new LogInsertModule(SessionUtils.getUserId(), "/test-plan/api/case/sort", HttpMethodConstants.POST.name()));
    }

    @PostMapping("/page")
    @Operation(summary = "测试计划-已关联接口用例列表分页查询")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public Pager<List<TestPlanApiCasePageResponse>> page(@Validated @RequestBody TestPlanApiCaseRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString("id")) ? request.getSortString("id") : "t.pos asc");
        return PageUtils.setPageInfo(page, testPlanApiCaseService.hasRelateApiCaseList(request, false));
    }


    @PostMapping("/module/count")
    @Operation(summary = "测试计划-已关联接口用例模块数量")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public Map<String, Long> moduleCount(@Validated @RequestBody TestPlanApiCaseModuleRequest request) {
        return testPlanApiCaseService.moduleCount(request);
    }

    @PostMapping("/tree")
    @Operation(summary = "测试计划-已关联接口用例列表模块树")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public List<BaseTreeNode> getTree(@Validated @RequestBody TestPlanApiCaseTreeRequest request) {
        return testPlanApiCaseService.getTree(request);
    }

    @PostMapping("/disassociate")
    @Operation(summary = "测试计划-计划详情-接口用例列表-取消关联用例")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_ASSOCIATION)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public TestPlanAssociationResponse disassociate(@Validated @RequestBody TestPlanDisassociationRequest request) {
        TestPlanApiCaseBatchRequest batchRequest = new TestPlanApiCaseBatchRequest();
        batchRequest.setTestPlanId(request.getTestPlanId());
        batchRequest.setSelectIds(List.of(request.getId()));
        TestPlanAssociationResponse response = testPlanApiCaseService.disassociate(batchRequest, new LogInsertModule(SessionUtils.getUserId(), "/test-plan/api/case/disassociate", HttpMethodConstants.POST.name()));
        testPlanService.refreshTestPlanStatus(request.getTestPlanId());
        return response;
    }


    @PostMapping("/batch/disassociate")
    @Operation(summary = "测试计划-计划详情-列表-批量取消关联用例")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_ASSOCIATION)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public TestPlanAssociationResponse batchDisassociate(@Validated @RequestBody TestPlanApiCaseBatchRequest request) {
        if (CollectionUtils.isEmpty(request.getProtocols())) {
            return new TestPlanAssociationResponse();
        }
        TestPlanAssociationResponse response = testPlanApiCaseService.disassociate(request, new LogInsertModule(SessionUtils.getUserId(), "/test-plan/api/case/batch/disassociate", HttpMethodConstants.POST.name()));
        testPlanService.refreshTestPlanStatus(request.getTestPlanId());
        return response;
    }


    @PostMapping("/batch/update/executor")
    @Operation(summary = "测试计划-计划详情-接口用例列表-批量更新执行人")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.batchUpdateExecutor(#request)", msClass = TestPlanApiCaseLogService.class)
    public void batchUpdateExecutor(@Validated @RequestBody TestPlanApiCaseUpdateRequest request) {
        testPlanApiCaseService.batchUpdateExecutor(request);
    }

    @GetMapping("/run/{id}")
    @Operation(summary = "用例执行")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_EXECUTE)
//    @CheckOwner(resourceId = "#id", resourceType = "test_plan_api_case") todo
    public TaskRequestDTO run(@PathVariable String id,
                              @Schema(description = "报告ID，传了可以实时获取结果，不传则不支持实时获取")
                              @RequestParam(required = false) String reportId) {
        return testPlanApiCaseService.run(id, reportId, SessionUtils.getUserId());
    }


    @PostMapping("/batch/run")
    @Operation(summary = "批量执行")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_EXECUTE)
//    @CheckOwner(resourceId = "#request.getId()", resourceType = "test_plan_api_case") todo
    public void batchRun(@Validated @RequestBody TestPlanApiCaseBatchRunRequest request) {
        testPlanApiCaseBatchRunService.asyncBatchRun(request, SessionUtils.getUserId());
    }

    //TODO 批量移动 （计划集内）

    @GetMapping("/report/get/{id}")
    @Operation(summary = "测试计划-用例列表-执行结果获取")
    @CheckOwner(resourceId = "#id", resourceType = "api_report")
    @RequiresPermissions(value = {PermissionConstants.TEST_PLAN_REPORT_READ, PermissionConstants.TEST_PLAN_READ_EXECUTE}, logical = Logical.OR)
    public ApiReportDTO get(@PathVariable String id) {
        testPlanApiCaseService.checkReportIsTestPlan(id);
        return apiReportService.get(id);
    }

    @GetMapping("/report/get/detail/{reportId}/{stepId}")
    @Operation(summary = "测试计划-用例列表-执行结果获取-报告详情获取")
    @CheckOwner(resourceId = "#reportId", resourceType = "api_report")
    @RequiresPermissions(value = {PermissionConstants.TEST_PLAN_REPORT_READ, PermissionConstants.TEST_PLAN_READ_EXECUTE}, logical = Logical.OR)
    public List<ApiReportDetailDTO> getDetail(@PathVariable String reportId,
                                              @PathVariable String stepId) {
        testPlanApiCaseService.checkReportIsTestPlan(reportId);
        return apiReportService.getDetail(reportId, stepId);
    }


    @PostMapping("/batch/move")
    @Operation(summary = "测试计划-计划详情-接口用例-批量移动")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.batchMove(#request)", msClass = TestPlanApiCaseLogService.class)
    public void batchMove(@Validated @RequestBody TestPlanApiCaseBatchMoveRequest request) {
        testPlanApiCaseService.batchMove(request);
    }
}
