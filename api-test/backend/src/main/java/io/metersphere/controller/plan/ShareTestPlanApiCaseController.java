package io.metersphere.controller.plan;

import io.metersphere.api.dto.automation.TestPlanFailureApiDTO;
import io.metersphere.service.BaseShareInfoService;
import io.metersphere.service.plan.TestPlanApiCaseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/share/test/plan/api/case")
public class ShareTestPlanApiCaseController {

    @Resource
    BaseShareInfoService baseShareInfoService;
    @Resource
    TestPlanApiCaseService testPlanApiCaseService;

    @GetMapping("/list/failure/{shareId}/{planId}")
    public List<TestPlanFailureApiDTO> getApiFailureList(@PathVariable String shareId, @PathVariable String planId) {
        baseShareInfoService.validate(shareId, planId);
        return testPlanApiCaseService.getFailureCases(planId);
    }

    @GetMapping("/list/errorReport/{shareId}/{planId}")
    public List<TestPlanFailureApiDTO> getErrorReportApiCaseList(@PathVariable String shareId, @PathVariable String planId) {
        baseShareInfoService.validate(shareId, planId);
        return testPlanApiCaseService.getErrorReportCases(planId);
    }

    @GetMapping("/list/unExecute/{shareId}/{planId}")
    public List<TestPlanFailureApiDTO> getUnExecuteCases(@PathVariable String shareId, @PathVariable String planId) {
        baseShareInfoService.validate(shareId, planId);
        return testPlanApiCaseService.getUnExecuteCases(planId);
    }

    @GetMapping("/list/all/{shareId}/{planId}")
    public List<TestPlanFailureApiDTO> getApiAllList(@PathVariable String shareId, @PathVariable String planId) {
        baseShareInfoService.validate(shareId, planId);
        return testPlanApiCaseService.getAllCases(planId);
    }
}
