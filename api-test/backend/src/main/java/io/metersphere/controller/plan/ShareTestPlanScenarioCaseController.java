package io.metersphere.controller.plan;

import io.metersphere.api.dto.automation.ApiScenarioReportResult;
import io.metersphere.api.dto.automation.TestPlanScenarioDTO;
import io.metersphere.api.dto.automation.TestPlanScenarioDTO;
import io.metersphere.service.ShareInfoService;
import io.metersphere.service.plan.TestPlanScenarioCaseService;
import io.metersphere.service.scenario.ApiScenarioReportService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/share/test/plan/scenario/case")
public class ShareTestPlanScenarioCaseController {

    @Resource
    ShareInfoService shareInfoService;
    @Resource
    TestPlanScenarioCaseService testPlanScenarioCaseService;
    @Resource
    private ApiScenarioReportService apiReportService;

    @GetMapping("/list/failure/{shareId}/{planId}")
    public List<TestPlanScenarioDTO> getScenarioFailureList(@PathVariable String shareId, @PathVariable String planId) {
        shareInfoService.validate(shareId);
        return testPlanScenarioCaseService.getFailureCases(planId);
    }

    @GetMapping("/list/all/{shareId}/{planId}")
    public List<TestPlanScenarioDTO> getScenarioAllList(@PathVariable String shareId, @PathVariable String planId) {
        shareInfoService.validate(shareId);
        return testPlanScenarioCaseService.getAllCases(planId);
    }

    @GetMapping("/list/errorReport/{shareId}/{planId}")
    public List<TestPlanScenarioDTO> getScenarioErrorReportList(@PathVariable String shareId, @PathVariable String planId) {
        shareInfoService.validate(shareId);
        return testPlanScenarioCaseService.getErrorReportCases(planId);
    }

    @GetMapping("/list/unExecute/{shareId}/{planId}")
    public List<TestPlanScenarioDTO> getUnExecuteScenarioCases(@PathVariable String shareId, @PathVariable String planId) {
        shareInfoService.validate(shareId);
        return testPlanScenarioCaseService.getUnExecuteCases(planId);
    }


    @GetMapping("/get/{shareId}/{reportId}")
    public ApiScenarioReportResult get(@PathVariable String shareId, @PathVariable String reportId) {
        shareInfoService.validate(shareId);
        return apiReportService.get(reportId, true);
    }
}
