package io.metersphere.plan.controller;

import io.metersphere.plan.dto.TestPlanLoadCaseDTO;
import io.metersphere.plan.request.LoadCaseReportRequest;
import io.metersphere.plan.service.TestPlanLoadCaseService;
import io.metersphere.service.ShareInfoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/share/test/plan/load/case")
public class ShareTestPlanLoadCaseController {

    @Resource
    ShareInfoService shareInfoService;
    @Resource
    TestPlanLoadCaseService testPlanLoadCaseService;

    @GetMapping("/list/failure/{shareId}/{planId}")
    public List<TestPlanLoadCaseDTO> getLoadFailureCases(@PathVariable String shareId, @PathVariable String planId) {
        shareInfoService.validate(shareId);
        return testPlanLoadCaseService.getFailureCases(planId);
    }

    @GetMapping("/list/all/{shareId}/{planId}")
    public List<TestPlanLoadCaseDTO> getLoadAllCases(@PathVariable String shareId, @PathVariable String planId) {
        shareInfoService.validate(shareId);
        return testPlanLoadCaseService.getAllCases(planId);
    }

    @PostMapping("/report/exist/{shareId}")
    public Boolean isExistReport(@PathVariable String shareId, @RequestBody LoadCaseReportRequest request) {
        shareInfoService.validate(shareId);
        return testPlanLoadCaseService.isExistReport(request);
    }
}
