package io.metersphere.controller.plan;

import io.metersphere.api.dto.automation.TestPlanFailureScenarioDTO;
import io.metersphere.service.ShareInfoService;
import io.metersphere.service.plan.TestPlanScenarioCaseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/share/test/plan/scenario/case")
public class ShareTestPlanScenarioCaseController {

    @Resource
    ShareInfoService shareInfoService;
    @Resource
    TestPlanScenarioCaseService testPlanScenarioCaseService;

    @GetMapping("/list/failure/{shareId}/{planId}")
    public List<TestPlanFailureScenarioDTO> getScenarioFailureList(@PathVariable String shareId, @PathVariable String planId) {
        shareInfoService.validate(shareId, planId);
        return testPlanScenarioCaseService.getFailureCases(planId);
    }

    @GetMapping("/list/all/{shareId}/{planId}")
    public List<TestPlanFailureScenarioDTO> getScenarioAllList(@PathVariable String shareId, @PathVariable String planId) {
        shareInfoService.validate(shareId, planId);
        return testPlanScenarioCaseService.getAllCases(planId);
    }

    @GetMapping("/list/errorReport/{shareId}/{planId}")
    public List<TestPlanFailureScenarioDTO> getScenarioErrorReportList(@PathVariable String shareId, @PathVariable String planId) {
        shareInfoService.validate(shareId, planId);
        return testPlanScenarioCaseService.getErrorReportCases(planId);
    }

    @GetMapping("/list/unExecute/{shareId}/{planId}")
    public List<TestPlanFailureScenarioDTO> getUnExecuteScenarioCases(@PathVariable String shareId, @PathVariable String planId) {
        shareInfoService.validate(shareId, planId);
        return testPlanScenarioCaseService.getUnExecuteCases(planId);
    }
}
