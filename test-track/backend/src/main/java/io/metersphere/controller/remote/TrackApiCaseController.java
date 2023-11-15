package io.metersphere.controller.remote;

import io.metersphere.dto.ApiCaseRelevanceRequest;
import io.metersphere.plan.dto.ApiTestCaseDTO;
import io.metersphere.plan.service.TestPlanService;
import io.metersphere.service.remote.api.RelevanceApiCaseService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

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

    @GetMapping("/{id}")
    public ApiTestCaseDTO get(@PathVariable String id) {
        return relevanceApiCaseService.getApiTestCaseDTO(id);
    }

}
