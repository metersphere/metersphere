package io.metersphere.controller.remote;

import io.metersphere.plan.dto.ApiModuleDTO;
import io.metersphere.plan.service.TestPlanProjectService;
import io.metersphere.plan.service.remote.api.PlanTestPlanApiCaseService;
import io.metersphere.service.wapper.CheckPermissionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("/test/plan/api/case")
@RestController
public class TrackTestPlanApiCaseController {

    @Resource
    PlanTestPlanApiCaseService planTestPlanApiCaseService;
    @Resource
    CheckPermissionService checkPermissionService;
    @Resource
    TestPlanProjectService testPlanProjectService;

    @GetMapping("/list/module/{planId}/{protocol}")
    public List<ApiModuleDTO> getNodeByPlanId(@PathVariable String planId, @PathVariable String protocol) {
        checkPermissionService.checkTestPlanOwner(planId);
        List<String> projectIds = testPlanProjectService.getProjectIdsByPlanId(planId);
        return planTestPlanApiCaseService.getNodeByPlanId(projectIds, planId, protocol);
    }
}
