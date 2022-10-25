package io.metersphere.controller.remote;

import io.metersphere.dto.ModuleNodeDTO;
import io.metersphere.plan.request.api.ApiScenarioRequest;
import io.metersphere.plan.service.TestPlanProjectService;
import io.metersphere.plan.service.remote.ui.PlanTestPlanUiScenarioCaseService;
import io.metersphere.service.wapper.CheckPermissionService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("/test/plan/uiScenario/case")
@RestController
public class TrackTestPlanUiScenarioCaseController {

    @Resource
    PlanTestPlanUiScenarioCaseService planTestPlanUiScenarioCaseService;
    @Resource
    CheckPermissionService checkPermissionService;
    @Resource
    TestPlanProjectService testPlanProjectService;

    @GetMapping("/list/module/{planId}")
    public List<ModuleNodeDTO> getNodeByPlanId(@PathVariable String planId) {
        checkPermissionService.checkTestPlanOwner(planId);
        List<String> projectIds = testPlanProjectService.getProjectIdsByPlanId(planId);
        return planTestPlanUiScenarioCaseService.getNodeByPlanId(projectIds, planId);
    }

    @PostMapping("/relevance/list/{goPage}/{pageSize}")
    public Object relevanceList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiScenarioRequest request) {
        return planTestPlanUiScenarioCaseService.relevanceList(request, goPage, pageSize);
    }
}
