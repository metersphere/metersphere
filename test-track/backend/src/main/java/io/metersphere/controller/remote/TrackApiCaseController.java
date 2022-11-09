package io.metersphere.controller.remote;

import io.metersphere.dto.ApiCaseRelevanceRequest;
import io.metersphere.plan.service.TestPlanService;
import io.metersphere.service.remote.api.RelevanceApiCaseService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RequestMapping("/api/testcase")
@RestController
public class TrackApiCaseController {

    @Resource
    RelevanceApiCaseService relevanceApiCaseService;
    @Resource
    TestPlanService testPlanService;

    @PostMapping("/relevance")
    public void relevance(@RequestBody ApiCaseRelevanceRequest request) {
        request.setAllowedRepeatCase(testPlanService.isAllowedRepeatCase(request.getPlanId()));
        relevanceApiCaseService.relevance(request);
    }
}
