package io.metersphere.functional.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.dto.TestCaseProviderDTO;
import io.metersphere.functional.request.FunctionalTestCaseDisassociateRequest;
import io.metersphere.functional.service.FunctionalTestCaseService;
import io.metersphere.request.ApiModuleProviderRequest;
import io.metersphere.request.TestCasePageProviderRequest;
import io.metersphere.request.AssociateOtherCaseRequest;
import io.metersphere.sdk.constants.PermissionConstants;
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

    @PostMapping("/associate/api/page")
    @Operation(summary = "用例管理-功能用例-关联其他用例-获取接口用例列表")
    @RequiresPermissions(value = {PermissionConstants.FUNCTIONAL_CASE_READ_ADD, PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE, PermissionConstants.FUNCTIONAL_CASE_READ_DELETE}, logical = Logical.OR)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<TestCaseProviderDTO>> associateApiCaseList(@Validated @RequestBody TestCasePageProviderRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, functionalTestCaseService.page(request));
    }

    @PostMapping("/associate/api/module/count")
    @Operation(summary = "用例管理-功能用例-关联其他用例-统计接口用例模块数量")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    @CheckOwner(resourceId = "#request.projectId", resourceType = "project")
    public Map<String, Long> moduleCount(@Validated @RequestBody ApiModuleProviderRequest request) {
        return functionalTestCaseService.moduleCount(request, false);
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
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    @CheckOwner(resourceId = "#request.projectId", resourceType = "project")
    public void disassociateCase(@Validated @RequestBody FunctionalTestCaseDisassociateRequest request) {
         functionalTestCaseService.disassociateCase(request);
    }







}
