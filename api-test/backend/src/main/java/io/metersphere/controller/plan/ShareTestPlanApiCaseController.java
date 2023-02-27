package io.metersphere.controller.plan;

import io.metersphere.api.dto.automation.TestPlanApiDTO;
import io.metersphere.service.ShareInfoService;
import io.metersphere.service.plan.TestPlanApiCaseService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/share/test/plan/api/case")
public class ShareTestPlanApiCaseController {

    @Resource
    ShareInfoService shareInfoService;
    @Resource
    TestPlanApiCaseService testPlanApiCaseService;

    @GetMapping("/list/failure/{shareId}/{planId}")
    public List<TestPlanApiDTO> getApiFailureList(@PathVariable String shareId, @PathVariable String planId) {
        shareInfoService.validate(shareId);
        return testPlanApiCaseService.getFailureCases(planId);
    }

    @GetMapping("/list/errorReport/{shareId}/{planId}")
    public List<TestPlanApiDTO> getErrorReportApiCaseList(@PathVariable String shareId, @PathVariable String planId) {
        shareInfoService.validate(shareId);
        return testPlanApiCaseService.getErrorReportCases(planId);
    }

    @GetMapping("/list/unExecute/{shareId}/{planId}")
    public List<TestPlanApiDTO> getUnExecuteCases(@PathVariable String shareId, @PathVariable String planId) {
        shareInfoService.validate(shareId);
        return testPlanApiCaseService.getUnExecuteCases(planId);
    }

    @GetMapping("/list/all/{shareId}/{planId}")
    public List<TestPlanApiDTO> getApiAllList(@PathVariable String shareId, @PathVariable String planId) {
        shareInfoService.validate(shareId);
        return testPlanApiCaseService.getAllCases(planId);
    }
}
