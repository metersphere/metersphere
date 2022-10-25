package io.metersphere.controller.remote;

import io.metersphere.plan.dto.ApiModuleDTO;
import io.metersphere.plan.request.api.ApiTestCaseRequest;
import io.metersphere.plan.service.TestPlanProjectService;
import io.metersphere.plan.service.remote.api.PlanTestPlanApiCaseService;
import io.metersphere.service.wapper.CheckPermissionService;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/relevance/list/{goPage}/{pageSize}")
    public Object relevanceList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiTestCaseRequest request) {
        return planTestPlanApiCaseService.relevanceList(goPage, pageSize, request);
    }
}
