package io.metersphere.functional.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.dto.BugProviderDTO;
import io.metersphere.dto.TestCaseProviderDTO;
import io.metersphere.functional.dto.FunctionalCaseTestDTO;
import io.metersphere.functional.dto.FunctionalCaseTestPlanDTO;
import io.metersphere.functional.dto.TestPlanCaseExecuteHistoryDTO;
import io.metersphere.functional.request.AssociatePlanPageRequest;
import io.metersphere.functional.request.DisassociateOtherCaseRequest;
import io.metersphere.functional.request.FunctionalCaseTestRequest;
import io.metersphere.functional.service.FunctionalCaseLogService;
import io.metersphere.functional.service.FunctionalTestCaseService;
import io.metersphere.request.*;
import io.metersphere.sdk.constants.PermissionConstants;
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
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "用例管理-功能用例-关联其他用例")
@RestController
@RequestMapping("/functional/case/test")
public class FunctionalTestCaseController {

    @Resource
    private FunctionalTestCaseService functionalTestCaseService;

    @PostMapping("/associate/case/page")
    @Operation(summary = "用例管理-功能用例-关联其他用例-获取需要关联的用例列表")
    @RequiresPermissions(value = {PermissionConstants.FUNCTIONAL_CASE_READ, PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE, PermissionConstants.FUNCTIONAL_CASE_READ_DELETE}, logical = Logical.OR)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<TestCaseProviderDTO>> associateOtherCaseList(@Validated @RequestBody TestCasePageProviderRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        return PageUtils.setPageInfo(page, functionalTestCaseService.page(request));
    }

    @PostMapping("/associate/case/module/count")
    @Operation(summary = "用例管理-功能用例-关联其他用例-统计需要关联用例模块数量")
    @RequiresPermissions(value = {PermissionConstants.FUNCTIONAL_CASE_READ, PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE, PermissionConstants.FUNCTIONAL_CASE_READ_DELETE}, logical = Logical.OR)
    @CheckOwner(resourceId = "#request.projectId", resourceType = "project")
    public Map<String, Long> moduleCount(@Validated @RequestBody TestCasePageProviderRequest request) {
        return functionalTestCaseService.moduleCount(request, false);
    }

    @PostMapping("/associate/case/module/tree")
    @Operation(summary = "用例管理-功能用例-关联其他用例-获取需要关联的用例模块树")
    @RequiresPermissions(value = {PermissionConstants.FUNCTIONAL_CASE_READ, PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE, PermissionConstants.FUNCTIONAL_CASE_READ_DELETE}, logical = Logical.OR)
    @CheckOwner(resourceId = "#request.projectId", resourceType = "project")
    public List<BaseTreeNode> getTree(@RequestBody @Validated AssociateCaseModuleRequest request) {
        return functionalTestCaseService.getTree(request);
    }

    @PostMapping("/associate/case")
    @Operation(summary = "用例管理-功能用例-关联其他用例-关联用例")
    @RequiresPermissions(value = {PermissionConstants.FUNCTIONAL_CASE_READ_ADD, PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE, PermissionConstants.FUNCTIONAL_CASE_READ_DELETE}, logical = Logical.OR)
    @CheckOwner(resourceId = "#request.sourceId", resourceType = "functional_case")
    public void associateCase(@Validated @RequestBody AssociateOtherCaseRequest request) {
        functionalTestCaseService.associateCase(request, false, SessionUtils.getUserId());
    }

    @PostMapping("/disassociate/case")
    @Operation(summary = "用例管理-功能用例-关联其他用例-取消关联用例")
    @Log(type = OperationLogType.DISASSOCIATE, expression = "#msClass.disassociateCaseLog(#request)", msClass = FunctionalCaseLogService.class)
    @RequiresPermissions(value = {PermissionConstants.FUNCTIONAL_CASE_READ_ADD, PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE, PermissionConstants.FUNCTIONAL_CASE_READ_DELETE}, logical = Logical.OR)
    @CheckOwner(resourceId = "#request.caseId", resourceType = "functional_case")
    public void disassociateCase(@Validated @RequestBody DisassociateOtherCaseRequest request) {
        functionalTestCaseService.disassociateCase(request);
    }


    @PostMapping("/has/associate/case/page")
    @Operation(summary = "用例管理-功能用例-关联其他用例-获取已关联的用例列表")
    @RequiresPermissions(value = {PermissionConstants.FUNCTIONAL_CASE_READ, PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE, PermissionConstants.FUNCTIONAL_CASE_READ_DELETE}, logical = Logical.OR)
    @CheckOwner(resourceId = "#request.sourceId", resourceType = "functional_case")
    public Pager<List<FunctionalCaseTestDTO>> getAssociateOtherCaseList(@Validated @RequestBody FunctionalCaseTestRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        return PageUtils.setPageInfo(page, functionalTestCaseService.hasAssociatePage(request));
    }


    @PostMapping("/associate/bug/page")
    @Operation(summary = "用例管理-功能用例-关联其他用例-获取缺陷列表")
    @RequiresPermissions(value = {PermissionConstants.FUNCTIONAL_CASE_READ, PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE, PermissionConstants.FUNCTIONAL_CASE_READ_DELETE}, logical = Logical.OR)
    @CheckOwner(resourceId = "#request.getProjectId", resourceType = "project")
    public Pager<List<BugProviderDTO>> associateBugList(@Validated @RequestBody BugPageProviderRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        return PageUtils.setPageInfo(page, functionalTestCaseService.bugPage(request));
    }

    @PostMapping("/associate/bug")
    @Operation(summary = "用例管理-功能用例-关联其他用例-关联缺陷")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_ADD)
    @CheckOwner(resourceId = "#request.caseId", resourceType = "functional_case")
    public void associateBug(@Validated @RequestBody AssociateBugRequest request) {
        functionalTestCaseService.associateBug(request, false, SessionUtils.getUserId());
    }

    @GetMapping("/disassociate/bug/{id}")
    @Operation(summary = "用例管理-功能用例-关联其他用例-取消关联缺陷")
    @Log(type = OperationLogType.DISASSOCIATE, expression = "#msClass.disassociateBugLog(#id)", msClass = FunctionalCaseLogService.class)
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_ADD)
    public void disassociateBug(@PathVariable String id) {
        functionalTestCaseService.disassociateBug(id);
    }


    @PostMapping("/has/associate/bug/page")
    @Operation(summary = "用例管理-功能用例-关联其他用例-获取已关联的缺陷列表")
    @RequiresPermissions(value = {PermissionConstants.FUNCTIONAL_CASE_READ, PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE, PermissionConstants.FUNCTIONAL_CASE_READ_DELETE}, logical = Logical.OR)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<BugProviderDTO>> getAssociateBugList(@Validated @RequestBody AssociateBugPageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        return PageUtils.setPageInfo(page, functionalTestCaseService.hasAssociateBugPage(request));
    }


    @PostMapping("/has/associate/plan/page")
    @Operation(summary = "用例管理-功能用例-关联其他用例-获取已关联的测试计划列表")
    @RequiresPermissions(value = {PermissionConstants.FUNCTIONAL_CASE_READ, PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE, PermissionConstants.FUNCTIONAL_CASE_READ_DELETE}, logical = Logical.OR)
    @CheckOwner(resourceId = "#request.caseId", resourceType = "functional_case")
    public Pager<List<FunctionalCaseTestPlanDTO>> getAssociateOtherPlanList(@Validated @RequestBody AssociatePlanPageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        return PageUtils.setPageInfo(page, functionalTestCaseService.hasAssociatePlanPage(request));
    }

    @GetMapping("/plan/comment/{caseId}")
    @Operation(summary = "用例管理-功能用例-测试计划-获取执行评论历史")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    @CheckOwner(resourceId = "#caseId", resourceType = "functional_case")
    public List<TestPlanCaseExecuteHistoryDTO> getTestPlanCaseExecuteHistory(@PathVariable String caseId) {
        return functionalTestCaseService.getTestPlanCaseExecuteHistory(caseId);
    }
}
