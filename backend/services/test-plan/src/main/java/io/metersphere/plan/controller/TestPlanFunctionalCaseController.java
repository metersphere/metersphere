package io.metersphere.plan.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.bug.domain.Bug;
import io.metersphere.bug.dto.request.BugEditRequest;
import io.metersphere.bug.service.BugLogService;
import io.metersphere.bug.service.BugService;
import io.metersphere.dto.BugProviderDTO;
import io.metersphere.plan.constants.TestPlanResourceConfig;
import io.metersphere.plan.dto.request.*;
import io.metersphere.plan.dto.response.*;
import io.metersphere.plan.service.TestPlanCaseLogService;
import io.metersphere.plan.service.TestPlanFunctionalCaseService;
import io.metersphere.plan.service.TestPlanManagementService;
import io.metersphere.plan.service.TestPlanService;
import io.metersphere.request.AssociateBugPageRequest;
import io.metersphere.request.BugPageProviderRequest;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.dto.LogInsertModule;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.user.UserDTO;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "测试计划功能用例")
@RequestMapping("/test-plan/functional/case")
public class TestPlanFunctionalCaseController {

    @Resource
    private TestPlanService testPlanService;
    @Resource
    private TestPlanManagementService testPlanManagementService;
    @Resource
    private TestPlanFunctionalCaseService testPlanFunctionalCaseService;
    @Resource
    private TestPlanCaseLogService testPlanCaseLogService;
    @Resource
    private BugService bugService;
    @Resource
    private BugLogService bugLogService;

    @PostMapping(value = "/sort")
    @Operation(summary = "测试计划功能用例-功能用例拖拽排序")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public TestPlanOperationResponse sortNode(@Validated @RequestBody ResourceSortRequest request) {
        return testPlanFunctionalCaseService.sortNode(request, new LogInsertModule(SessionUtils.getUserId(), "/test-plan/functional/case/sort", HttpMethodConstants.POST.name()));
    }

    @PostMapping("/page")
    @Operation(summary = "测试计划-已关联功能用例分页查询")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public Pager<List<TestPlanCasePageResponse>> page(@Validated @RequestBody TestPlanCaseRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString("id", "functional_case")) ? request.getSortString("id", "functional_case") : "test_plan_functional_case.pos desc");
        return PageUtils.setPageInfo(page, testPlanFunctionalCaseService.getFunctionalCasePage(request, false, SessionUtils.getCurrentProjectId()));
    }

    @PostMapping("/tree")
    @Operation(summary = "测试计划-已关联功能用例列表模块树")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ)
    @CheckOwner(resourceId = "#testPlanId", resourceType = "test_plan")
    public List<BaseTreeNode> getTree(@Validated @RequestBody TestPlanTreeRequest request) {
        return testPlanFunctionalCaseService.getTree(request);
    }

    @PostMapping("/module/count")
    @Operation(summary = "测试计划-已关联功能用例模块数量")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public Map<String, Long> moduleCount(@Validated @RequestBody TestPlanCaseModuleRequest request) {
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
        TestPlanAssociationResponse response = testPlanFunctionalCaseService.disassociate(batchRequest, new LogInsertModule(SessionUtils.getUserId(), "/test-plan/functional/case/disassociate", HttpMethodConstants.POST.name()));
        return response;
    }

    @PostMapping("/batch/disassociate")
    @Operation(summary = "测试计划-计划详情-列表-批量取消关联用例")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_ASSOCIATION)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public TestPlanAssociationResponse batchDisassociate(@Validated @RequestBody BasePlanCaseBatchRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getTestPlanId(), TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN_FUNCTIONAL_CASE));
        TestPlanAssociationResponse response = testPlanFunctionalCaseService.disassociate(request, new LogInsertModule(SessionUtils.getUserId(), "/test-plan/functional/case/batch/disassociate", HttpMethodConstants.POST.name()));
        return response;
    }

    @PostMapping("/associate/bug/page")
    @Operation(summary = "测试计划-计划详情-功能用例-获取待关联缺陷列表")
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ)
    public Pager<List<BugProviderDTO>> associateBugList(@Validated @RequestBody BugPageProviderRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        return PageUtils.setPageInfo(page, testPlanFunctionalCaseService.bugPage(request));
    }

    @PostMapping("/associate/bug")
    @Operation(summary = "测试计划-计划详情-功能用例-关联其他用例-关联缺陷")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_EXECUTE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void associateBug(@Validated @RequestBody TestPlanCaseAssociateBugRequest request) {
        testPlanFunctionalCaseService.associateBug(request, SessionUtils.getUserId());
    }

    @GetMapping("/disassociate/bug/{id}")
    @Operation(summary = "用例管理-功能用例-关联其他用例-取消关联缺陷")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_EXECUTE)
    @Log(type = OperationLogType.DISASSOCIATE, expression = "#msClass.disassociateBugLog(#id)", msClass = TestPlanFunctionalCaseService.class)
    public void disassociateBug(@PathVariable String id) {
        testPlanFunctionalCaseService.disassociateBug(id);
    }

    @PostMapping("/run")
    @Operation(summary = "测试计划-计划详情-功能用例-执行")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_EXECUTE)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public void run(@Validated @RequestBody TestPlanCaseRunRequest request) {
        testPlanFunctionalCaseService.run(request, new LogInsertModule(SessionUtils.getUserId(), "/test-plan/functional/case/run", HttpMethodConstants.POST.name()));
        testPlanService.setActualStartTime(request.getTestPlanId());
    }

    @PostMapping("/batch/run")
    @Operation(summary = "测试计划-计划详情-功能用例-批量执行")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_EXECUTE)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public void batchRun(@Validated @RequestBody TestPlanCaseBatchRunRequest request) {
        testPlanFunctionalCaseService.batchRun(request, new LogInsertModule(SessionUtils.getUserId(), "/test-plan/functional/case/batch/run", HttpMethodConstants.POST.name()));
        testPlanService.setActualStartTime(request.getTestPlanId());
    }

    @PostMapping("/has/associate/bug/page")
    @Operation(summary = "测试计划-计划详情-功能用例-获取已关联的缺陷列表")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_EXECUTE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<BugProviderDTO>> getAssociateBugList(@Validated @RequestBody AssociateBugPageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        return PageUtils.setPageInfo(page, testPlanFunctionalCaseService.hasAssociateBugPage(request));
    }

    @PostMapping("/batch/update/executor")
    @Operation(summary = "测试计划-计划详情-功能用例-批量更新执行人")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public void batchUpdateExecutor(@Validated @RequestBody TestPlanCaseUpdateRequest request) {
        testPlanFunctionalCaseService.batchUpdateExecutor(request);
        testPlanCaseLogService.batchUpdateExecutor(request);
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "测试计划-计划详情-功能用例-获取用例详情")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    public TestPlanCaseDetailResponse getFunctionalCaseDetail(@PathVariable String id) {
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

    @GetMapping("/user-option/{projectId}")
    @Operation(summary = "测试计划-计划详情-功能用例-获取用户列表")
    @RequiresPermissions(value = {PermissionConstants.TEST_PLAN_READ, PermissionConstants.TEST_PLAN_READ_UPDATE, PermissionConstants.TEST_PLAN_READ_ADD}, logical = Logical.OR)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<UserDTO> getReviewUserList(@PathVariable String projectId, @Schema(description = "查询关键字，根据邮箱和用户名查询")
    @RequestParam(value = "keyword", required = false) String keyword) {
        return testPlanFunctionalCaseService.getExecUserList(projectId, keyword);
    }

    @PostMapping("/batch/move")
    @Operation(summary = "测试计划-计划详情-功能用例-批量移动")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public void batchMove(@Validated @RequestBody BaseBatchMoveRequest request) {
        testPlanFunctionalCaseService.batchMove(request);
        testPlanCaseLogService.batchMove(request, SessionUtils.getUserId());
    }


    @PostMapping("/batch/add-bug")
    @Operation(summary = "测试计划-计划详情-功能用例-批量添加缺陷")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_EXECUTE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void batchAddBug(@Validated @RequestPart("request") TestPlanCaseBatchAddBugRequest request,
                            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        BugEditRequest bugEditRequest = new BugEditRequest();
        BeanUtils.copyBean(bugEditRequest, request);
        Bug bug = bugService.addOrUpdate(bugEditRequest, files, SessionUtils.getUserId(), SessionUtils.getCurrentOrganizationId(), false);
        bugLogService.minderAddLog(bugEditRequest, files, SessionUtils.getCurrentOrganizationId(), bug.getId(), SessionUtils.getUserId());
        testPlanFunctionalCaseService.batchAssociateBug(request, bug.getId(), SessionUtils.getUserId());
    }


    @PostMapping("/batch/associate-bug")
    @Operation(summary = "测试计划-计划详情-功能用例-批量关联缺陷")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_EXECUTE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void batchAssociateBug(@Validated @RequestBody TestPlanCaseBatchAssociateBugRequest request) {
        testPlanFunctionalCaseService.batchAssociateBugByIds(request, SessionUtils.getUserId());
    }

}
