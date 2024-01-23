package io.metersphere.plan.controller;

import io.metersphere.plan.constants.TestPlanResourceConfig;
import io.metersphere.plan.dto.LogInsertModule;
import io.metersphere.plan.dto.request.ResourceSortRequest;
import io.metersphere.plan.dto.request.TestPlanAssociationRequest;
import io.metersphere.plan.dto.response.TestPlanAssociationResponse;
import io.metersphere.plan.dto.response.TestPlanResourceSortResponse;
import io.metersphere.plan.service.TestPlanFunctionalCaseService;
import io.metersphere.plan.service.TestPlanManagementService;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@Tag(name = "测试计划功能用例")
@RequestMapping("/test-plan/functional/case")
public class TestPlanFunctionalCaseController {

    @Resource
    private TestPlanManagementService testPlanManagementService;
    @Resource
    private TestPlanFunctionalCaseService testPlanFunctionalCaseService;

    @PostMapping(value = "/association")
    @Operation(summary = "测试计划功能用例-关联功能用例")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_ASSOCIATION)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public TestPlanAssociationResponse association(@Validated @RequestBody TestPlanAssociationRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getTestPlanId(), TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN_FUNCTIONAL_CASE));
        return testPlanFunctionalCaseService.association(request, new LogInsertModule(SessionUtils.getUserId(), "/test-plan/functional/case/association", HttpMethodConstants.POST.name()));
    }

    @PostMapping(value = "/sort")
    @Operation(summary = "测试计划功能用例-关联功能用例")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public TestPlanResourceSortResponse sortNode(@Validated @RequestBody ResourceSortRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getTestPlanId(), TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN_FUNCTIONAL_CASE));
        return testPlanFunctionalCaseService.sortNode(request, new LogInsertModule(SessionUtils.getUserId(), "/test-plan/functional/case/sort", HttpMethodConstants.POST.name()));
    }

}
