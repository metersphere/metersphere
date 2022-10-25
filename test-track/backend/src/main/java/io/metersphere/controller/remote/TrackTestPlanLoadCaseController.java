package io.metersphere.controller.remote;

import io.metersphere.plan.request.performance.LoadCaseRequest;
import io.metersphere.plan.service.remote.performance.PlanTestPlanLoadCaseService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RequestMapping("/test/plan/load/case")
@RestController
public class TrackTestPlanLoadCaseController {

    @Resource
    PlanTestPlanLoadCaseService planTestPlanLoadCaseService;

    @PostMapping("/relevance/list/{goPage}/{pageSize}")
    public Object relevanceList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody LoadCaseRequest request) {
        return planTestPlanLoadCaseService.relevanceList(goPage, pageSize, request);
    }
}
