package io.metersphere.plan.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.dto.BugProviderDTO;
import io.metersphere.functional.dto.FunctionalCaseDetailDTO;
import io.metersphere.plan.constants.TestPlanResourceConfig;
import io.metersphere.plan.dto.request.*;
import io.metersphere.plan.dto.response.TestPlanAssociationResponse;
import io.metersphere.plan.dto.response.TestPlanCaseExecHistoryResponse;
import io.metersphere.plan.dto.response.TestPlanCasePageResponse;
import io.metersphere.plan.dto.response.TestPlanResourceSortResponse;
import io.metersphere.plan.service.TestPlanCaseLogService;
import io.metersphere.plan.service.TestPlanFunctionalCaseService;
import io.metersphere.plan.service.TestPlanManagementService;
import io.metersphere.request.AssociateBugPageRequest;
import io.metersphere.request.BugPageProviderRequest;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.PermissionConstants;
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
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "测试计划功能用例")
@RequestMapping("/test-plan/functional/case")
public class TestPlanFunctionalCaseController {

    @Resource
    private TestPlanManagementService testPlanManagementService;
    @Resource
    private TestPlanFunctionalCaseService testPlanFunctionalCaseService;

    @PostMapping(value = "/sort")
    @Operation(summary = "测试计划功能用例-功能用例拖拽排序")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public TestPlanResourceSortResponse sortNode(@Validated @RequestBody ResourceSortRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getTestPlanId(), TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN_FUNCTIONAL_CASE));
        return testPlanFunctionalCaseService.sortNode(request, new LogInsertModule(SessionUtils.getUserId(), "/test-plan/functional/case/sort", HttpMethodConstants.POST.name()));
    }

    @PostMapping("/page")
    @Operation(summary = "测试计划-已关联功能用例分页查询")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public Pager<List<TestPlanCasePageResponse>> page(@Validated @RequestBody TestPlanCaseRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        return PageUtils.setPageInfo(page, testPlanFunctionalCaseService.getFunctionalCasePage(request, false));
    }

    @GetMapping("/tree/{testPlanId}")
    @Operation(summary = "测试计划-已关联功能用例列表模块树")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ)
    @CheckOwner(resourceId = "#testPlanId", resourceType = "test_plan")
    public List<BaseTreeNode> getTree(@PathVariable String testPlanId) {
        return testPlanFunctionalCaseService.getTree(testPlanId);
    }

    @PostMapping("/module/count")
    @Operation(summary = "测试计划-已关联功能用例模块数量")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public Map<String, Long> moduleCount(@Validated @RequestBody TestPlanCaseRequest request) {
        return testPlanFunctionalCaseService.moduleCount(request);
    }

    @PostMapping("/disassociate")
    @Operation(summary = "测试计划-计划详情-列表-取消关联用例")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_ASSOCIATION)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public TestPlanAssociationResponse disassociate(@Validated @RequestBody TestPlanDisassociationRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getTestPlanId(), TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN_FUNCTIONAL_CASE));
        BasePlanCaseBatchRequest batchRequest = new BasePlanCaseBatchRequest();
        batchRequest.setTestPlanId(request.getTestPlanId());
        batchRequest.setSelectIds(List.of(request.getId()));
        return testPlanFunctionalCaseService.disassociate(batchRequest, new LogInsertModule(SessionUtils.getUserId(), "/test-plan/functional/case/association", HttpMethodConstants.POST.name()));
    }

    @PostMapping("/batch/disassociate")
    @Operation(summary = "测试计划-计划详情-列表-批量取消关联用例")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_ASSOCIATION)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public TestPlanAssociationResponse batchDisassociate(@Validated @RequestBody BasePlanCaseBatchRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getTestPlanId(), TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN_FUNCTIONAL_CASE));
        return testPlanFunctionalCaseService.disassociate(request, new LogInsertModule(SessionUtils.getUserId(), "/test-plan/functional/case/association", HttpMethodConstants.POST.name()));
    }

    @PostMapping("/associate/bug/page")
    @Operation(summary = "测试计划-计划详情-功能用例-获取缺陷列表")
    @CheckOwner(resourceId = "#request.getProjectId", resourceType = "project")
    public Pager<List<BugProviderDTO>> associateBugList(@Validated @RequestBody BugPageProviderRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        return PageUtils.setPageInfo(page, testPlanFunctionalCaseService.bugPage(request));
    }

    @PostMapping("/associate/bug")
    @Operation(summary = "测试计划-计划详情-功能用例-关联其他用例-关联缺陷")
    @CheckOwner(resourceId = "#request.getTestPlanCaseId()", resourceType = "test_plan_functional_case")
    public void associateBug(@Validated @RequestBody TestPlanCaseAssociateBugRequest request) {
        testPlanFunctionalCaseService.associateBug(request, SessionUtils.getUserId());
    }

    @GetMapping("/disassociate/bug/{id}")
    @Operation(summary = "用例管理-功能用例-关联其他用例-取消关联缺陷")
    @Log(type = OperationLogType.DISASSOCIATE, expression = "#msClass.disassociateBugLog(#id)", msClass = TestPlanFunctionalCaseService.class)
    @CheckOwner(resourceId = "#id", resourceType = "bug_relation_case")
    public void disassociateBug(@PathVariable String id) {
        testPlanFunctionalCaseService.disassociateBug(id);
    }

    @PostMapping("/run")
    @Operation(summary = "测试计划-计划详情-功能用例-执行")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_EXECUTE)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public void run(@Validated @RequestBody TestPlanCaseRunRequest request) {
        testPlanFunctionalCaseService.run(request, SessionUtils.getCurrentOrganizationId(), new LogInsertModule(SessionUtils.getUserId(), "/test-plan/functional/case/run", HttpMethodConstants.POST.name()));
    }


    @PostMapping("/batch/run")
    @Operation(summary = "测试计划-计划详情-功能用例-批量执行")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_EXECUTE)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public void batchRun(@Validated @RequestBody TestPlanCaseBatchRunRequest request) {
        testPlanFunctionalCaseService.batchRun(request, SessionUtils.getCurrentOrganizationId(), new LogInsertModule(SessionUtils.getUserId(), "/test-plan/functional/case/batch/run", HttpMethodConstants.POST.name()));
    }

    @PostMapping("/has/associate/bug/page")
    @Operation(summary = "测试计划-计划详情-功能用例-获取已关联的缺陷列表")
    @CheckOwner(resourceId = "#request.getTestPlanCaseId()", resourceType = "test_plan_functional_case")
    public Pager<List<BugProviderDTO>> getAssociateBugList(@Validated @RequestBody AssociateBugPageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        return PageUtils.setPageInfo(page, testPlanFunctionalCaseService.hasAssociateBugPage(request));
    }


    @PostMapping("/batch/update/executor")
    @Operation(summary = "测试计划-计划详情-功能用例-批量更新执行人")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    @Log(type = OperationLogType.DISASSOCIATE, expression = "#msClass.batchUpdateExecutor(#request)", msClass = TestPlanCaseLogService.class)
    public void batchUpdateExecutor(@Validated @RequestBody TestPlanCaseUpdateRequest request) {
        testPlanFunctionalCaseService.batchUpdateExecutor(request);
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "测试计划-计划详情-功能用例-获取用例详情")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    public FunctionalCaseDetailDTO getFunctionalCaseDetail(@PathVariable String id) {
        String userId = SessionUtils.getUserId();
        return testPlanFunctionalCaseService.getFunctionalCaseDetail(id, userId);
    }


    @PostMapping("/exec/history")
    @Operation(summary = "测试计划-计划详情-功能用例-执行历史")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public List<TestPlanCaseExecHistoryResponse> executeHistory(@Validated @RequestBody TestPlanCaseExecHistoryRequest request) {
        return testPlanFunctionalCaseService.getCaseExecHistory(request);
    }


    @PostMapping("/edit")
    @Operation(summary = "测试计划-计划详情-功能用例-编辑执行结果")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public void editFunctionalCase(@Validated @RequestBody TestPlanCaseEditRequest request) {
        testPlanFunctionalCaseService.editFunctionalCase(request, SessionUtils.getUserId());
    }
}
