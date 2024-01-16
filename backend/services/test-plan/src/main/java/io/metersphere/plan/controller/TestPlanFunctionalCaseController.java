package io.metersphere.plan.controller;

import io.metersphere.plan.dto.request.ResourceSortRequest;
import io.metersphere.plan.dto.request.TestPlanAssociationRequest;
import io.metersphere.plan.dto.response.TestPlanAssociationResponse;
import io.metersphere.plan.dto.response.TestPlanResourceSortResponse;
import io.metersphere.plan.service.TestPlanFunctionalCaseService;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test-plan/functional/case")
public class TestPlanFunctionalCaseController {

    @Resource
    private TestPlanFunctionalCaseService testPlanFunctionalCaseService;

    @PostMapping(value = "/association")
    @Operation(summary = "测试计划-关联功能用例")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public TestPlanAssociationResponse association(@Validated @RequestBody TestPlanAssociationRequest request) {
        return testPlanFunctionalCaseService.association(request, SessionUtils.getUserId(), "/test-plan/functional/case/association", HttpMethodConstants.POST.name());
    }

    @PostMapping(value = "/sort")
    @Operation(summary = "测试计划-关联功能用例")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public TestPlanResourceSortResponse sortNode(@Validated @RequestBody ResourceSortRequest request) {
        return testPlanFunctionalCaseService.sortNode(request, SessionUtils.getUserId(), "/test-plan/functional/case/sort", HttpMethodConstants.POST.name());
    }

}
