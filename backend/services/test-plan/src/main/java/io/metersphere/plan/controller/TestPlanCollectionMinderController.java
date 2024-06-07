package io.metersphere.plan.controller;

import io.metersphere.plan.dto.TestPlanCollectionMinderTreeDTO;
import io.metersphere.plan.service.TestPlanCollectionMinderService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.security.CheckOwner;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "测试规划脑图")
@RestController
@RequestMapping("/test-plan/mind")
public class TestPlanCollectionMinderController {

    @Resource
    private TestPlanCollectionMinderService testPlanCollectionMinderService;

    @GetMapping("/data/{planId}")
    @Operation(summary = "测试规划脑图列表")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_UPDATE)
    @CheckOwner(resourceId = "#planId", resourceType = "test_plan")
    public List<TestPlanCollectionMinderTreeDTO> getMindTestPlanCase(@PathVariable String planId) {
        return testPlanCollectionMinderService.getMindTestPlanCase(planId);
    }

}
