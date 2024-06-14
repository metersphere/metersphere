package io.metersphere.plan.controller;

import io.metersphere.plan.dto.TestPlanCollectionMinderTreeDTO;
import io.metersphere.plan.dto.request.TestPlanCollectionMinderEditRequest;
import io.metersphere.plan.service.TestPlanCollectionMinderService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "测试规划脑图")
@RestController
@RequestMapping("/test-plan/mind")
public class TestPlanCollectionMinderController {

    @Resource
    private TestPlanCollectionMinderService testPlanCollectionMinderService;

    @GetMapping("/data/{planId}")
    @Operation(summary = "测试规划脑图列表")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ)
    @CheckOwner(resourceId = "#planId", resourceType = "test_plan")
    public List<TestPlanCollectionMinderTreeDTO> getMindTestPlanCase(@PathVariable String planId) {
        return testPlanCollectionMinderService.getMindTestPlanCase(planId);
    }


    @PostMapping("/data/edit")
    @Operation(summary = "测试规划脑图列表")
    @RequiresPermissions(value = {
            PermissionConstants.TEST_PLAN_READ,
            PermissionConstants.TEST_PLAN_READ_ADD,
            PermissionConstants.TEST_PLAN_READ_UPDATE,
            PermissionConstants.TEST_PLAN_READ_DELETE,
            PermissionConstants.TEST_PLAN_READ_ASSOCIATION,
    }, logical = Logical.OR)
    @CheckOwner(resourceId = "#request.planId", resourceType = "test_plan")
    public void editMindTestPlanCase(@Validated @RequestBody TestPlanCollectionMinderEditRequest request) {
        testPlanCollectionMinderService.editMindTestPlanCase(request, SessionUtils.getUser());
    }


}
