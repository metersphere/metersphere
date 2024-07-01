package io.metersphere.plan.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.definition.ApiDefinitionDTO;
import io.metersphere.api.dto.definition.ApiModuleRequest;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.dto.scenario.ApiScenarioDTO;
import io.metersphere.functional.dto.FunctionalCasePageDTO;
import io.metersphere.functional.request.FunctionalCasePageRequest;
import io.metersphere.functional.service.FunctionalCaseService;
import io.metersphere.plan.dto.request.TestPlanApiCaseRequest;
import io.metersphere.plan.dto.request.TestPlanApiRequest;
import io.metersphere.plan.dto.request.TestPlanApiScenarioRequest;
import io.metersphere.plan.service.TestPlanApiCaseService;
import io.metersphere.plan.service.TestPlanApiScenarioService;
import io.metersphere.plan.service.TestPlanConfigService;
import io.metersphere.plan.service.TestPlanManagementService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "测试计划关联用例弹窗接口相关")
@RequestMapping("/test-plan/association")
public class TestPlanAssociateController {

    @Resource
    private TestPlanConfigService testPlanConfigService;
    @Resource
    private FunctionalCaseService functionalCaseService;
    @Resource
    private TestPlanApiCaseService testPlanApiCaseService;
    @Resource
    private TestPlanManagementService testPlanManagementService;
    @Resource
    private TestPlanApiScenarioService testPlanApiScenarioService;

    @PostMapping("/page")
    @Operation(summary = "测试计划-关联用例弹窗-功能用例列表查询(项目)")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public Pager<List<FunctionalCasePageDTO>> getFunctionalCasePage(@Validated @RequestBody FunctionalCasePageRequest request) {
        boolean moduleIsOpen = testPlanManagementService.checkModuleIsOpenByProjectId(request.getProjectId());
        if (!moduleIsOpen) {
            return new Pager<>();
        }
        boolean isRepeat = false;
        if (StringUtils.isNotBlank(request.getTestPlanId())) {
            isRepeat = testPlanConfigService.isRepeatCase(request.getTestPlanId());
        }
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "pos desc");
        return PageUtils.setPageInfo(page, functionalCaseService.getFunctionalCasePage(request, false, isRepeat));
    }

    @PostMapping("/api/page")
    @Operation(summary = "测试计划-关联用例弹窗-接口列表查询(项目)")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public Pager<List<ApiDefinitionDTO>> getApiPage(@Validated @RequestBody TestPlanApiRequest request) {
        boolean moduleIsOpen = testPlanManagementService.checkModuleIsOpenByProjectId(request.getProjectId());
        if (!moduleIsOpen) {
            return new Pager<>();
        }
        boolean isRepeat = testPlanConfigService.isRepeatCase(request.getTestPlanId());
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "pos desc");
        return PageUtils.setPageInfo(page, testPlanApiCaseService.getApiPage(request, isRepeat));
    }


    @PostMapping("/api/case/page")
    @Operation(summary = "测试计划-关联用例弹窗-接口CASE列表查询(项目)")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public Pager<List<ApiTestCaseDTO>> page(@Validated @RequestBody TestPlanApiCaseRequest request) {
        boolean moduleIsOpen = testPlanManagementService.checkModuleIsOpenByProjectId(request.getProjectId());
        if (!moduleIsOpen) {
            return new Pager<>();
        }
        boolean isRepeat = testPlanConfigService.isRepeatCase(request.getTestPlanId());
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString("id")) ? request.getSortString("id") : "pos desc, id desc");
        return PageUtils.setPageInfo(page, testPlanApiCaseService.getApiCasePage(request, isRepeat));
    }


    @PostMapping("/api/scenario/page")
    @Operation(summary = "测试计划-关联用例弹窗-接口场景列表查询(项目)")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public Pager<List<ApiScenarioDTO>> getApiScenarioPage(@Validated @RequestBody TestPlanApiScenarioRequest request) {
        boolean moduleIsOpen = testPlanManagementService.checkModuleIsOpenByProjectId(request.getProjectId());
        if (!moduleIsOpen) {
            return new Pager<>();
        }
        boolean isRepeat = testPlanConfigService.isRepeatCase(request.getTestPlanId());
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "pos desc");
        return PageUtils.setPageInfo(page, testPlanApiScenarioService.getApiScenarioPage(request, isRepeat));
    }


    @PostMapping("/api/case/module/count")
    @Operation(summary = "测试计划-关联用例弹窗-接口CASE模块数量(项目)")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public Map<String, Long> moduleCount(@Validated @RequestBody ApiModuleRequest request) {
        return testPlanApiCaseService.getApiCaseModuleCount(request, false);
    }

}
