package io.metersphere.controller.remote;

import io.metersphere.plan.dto.ApiScenarioModuleDTO;
import io.metersphere.plan.service.TestPlanProjectService;
import io.metersphere.plan.service.remote.api.PlanTestPlanScenarioCaseService;
import io.metersphere.service.wapper.CheckPermissionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("/test/plan/scenario/case")
@RestController
public class TrackTestPlanScenarioCaseController {

    @Resource
    PlanTestPlanScenarioCaseService planTestPlanScenarioCaseService;
    @Resource
    CheckPermissionService checkPermissionService;
    @Resource
    TestPlanProjectService testPlanProjectService;

    @GetMapping("/list/module/{planId}")
    public List<ApiScenarioModuleDTO> getNodeByPlanId(@PathVariable String planId) {
        checkPermissionService.checkTestPlanOwner(planId);
        List<String> projectIds = testPlanProjectService.getProjectIdsByPlanId(planId);
        return planTestPlanScenarioCaseService.getNodeByPlanId(projectIds, planId);
    }
}
