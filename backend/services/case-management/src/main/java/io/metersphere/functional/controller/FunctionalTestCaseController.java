package io.metersphere.functional.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.dto.TestCaseProviderDTO;
import io.metersphere.functional.dto.FunctionalCaseTestDTO;
import io.metersphere.functional.request.AssociateCaseModuleRequest;
import io.metersphere.functional.request.DisassociateOtherCaseRequest;
import io.metersphere.functional.request.FunctionalCaseTestRequest;
import io.metersphere.functional.service.FunctionalCaseLogService;
import io.metersphere.functional.service.FunctionalTestCaseService;
import io.metersphere.request.AssociateCaseModuleProviderRequest;
import io.metersphere.request.AssociateOtherCaseRequest;
import io.metersphere.request.TestCasePageProviderRequest;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @RequiresPermissions(value = {PermissionConstants.FUNCTIONAL_CASE_READ_ADD, PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE, PermissionConstants.FUNCTIONAL_CASE_READ_DELETE}, logical = Logical.OR)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<TestCaseProviderDTO>> associateOtherCaseList(@Validated @RequestBody TestCasePageProviderRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, functionalTestCaseService.page(request));
    }

    @PostMapping("/associate/case/module/count")
    @Operation(summary = "用例管理-功能用例-关联其他用例-统计需要关联用例模块数量")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    @CheckOwner(resourceId = "#request.projectId", resourceType = "project")
    public Map<String, Long> moduleCount(@Validated @RequestBody AssociateCaseModuleProviderRequest request) {
        return functionalTestCaseService.moduleCount(request, false);
    }

    @PostMapping("/associate/case/module/tree")
    @Operation(summary = "用例管理-功能用例-关联其他用例-查找模块")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    @CheckOwner(resourceId = "#request.projectId", resourceType = "project")
    public List<BaseTreeNode> getTree(@RequestBody @Validated AssociateCaseModuleRequest request) {
        return functionalTestCaseService.getTree(request);
    }

    @PostMapping("/associate/case")
    @Operation(summary = "用例管理-功能用例-关联其他用例-关联用例")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    @CheckOwner(resourceId = "#request.sourceId", resourceType = "functional_case")
    public void associateCase(@Validated @RequestBody AssociateOtherCaseRequest request) {
         functionalTestCaseService.associateCase(request, false, SessionUtils.getUserId());
    }

    @PostMapping("/disassociate/case")
    @Operation(summary = "用例管理-功能用例-关联其他用例-取消关联用例")
    @Log(type = OperationLogType.DISASSOCIATE, expression = "#msClass.disassociateCaseLog(#request)", msClass = FunctionalCaseLogService.class)
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    @CheckOwner(resourceId = "#request.projectId", resourceType = "project")
    public void disassociateCase(@Validated @RequestBody DisassociateOtherCaseRequest request) {
         functionalTestCaseService.disassociateCase(request);
    }


    @PostMapping("/has/associate/case/page")
    @Operation(summary = "用例管理-功能用例-关联其他用例-获取已关联的用例列表")
    @RequiresPermissions(value = {PermissionConstants.FUNCTIONAL_CASE_READ_ADD, PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE, PermissionConstants.FUNCTIONAL_CASE_READ_DELETE}, logical = Logical.OR)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<FunctionalCaseTestDTO>> getAssociateOtherCaseList(@Validated @RequestBody FunctionalCaseTestRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        return PageUtils.setPageInfo(page, functionalTestCaseService.hasAssociatePage(request));
    }



}
