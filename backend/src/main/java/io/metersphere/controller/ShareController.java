package io.metersphere.controller;

import io.metersphere.api.dto.APIReportResult;
import io.metersphere.api.dto.automation.APIScenarioReportResult;
import io.metersphere.api.dto.automation.TestPlanFailureApiDTO;
import io.metersphere.api.dto.automation.TestPlanFailureScenarioDTO;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.api.service.ApiScenarioReportService;
import io.metersphere.api.service.ShareInfoService;
import io.metersphere.base.domain.IssuesDao;
import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.performance.base.ReportTimeInfo;
import io.metersphere.performance.dto.LoadTestExportJmx;
import io.metersphere.performance.service.PerformanceReportService;
import io.metersphere.performance.service.PerformanceTestService;
import io.metersphere.track.dto.TestPlanCaseDTO;
import io.metersphere.track.dto.TestPlanLoadCaseDTO;
import io.metersphere.track.dto.TestPlanSimpleReportDTO;
import io.metersphere.track.request.testplan.LoadCaseReportRequest;
import io.metersphere.track.service.*;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.List;

@RestController
@RequestMapping("share")
public class ShareController {

    @Resource
    TestPlanService testPlanService;
    @Resource
    TestPlanTestCaseService testPlanTestCaseService;
    @Resource
    ApiDefinitionService apiDefinitionService;
    @Resource
    TestPlanApiCaseService testPlanApiCaseService;
    @Resource
    TestPlanScenarioCaseService testPlanScenarioCaseService;
    @Resource
    ApiScenarioReportService apiScenarioReportService;
    @Resource
    TestPlanLoadCaseService testPlanLoadCaseService;
    @Resource
    IssuesService issuesService;
    @Resource
    ShareInfoService shareInfoService;
    @Resource
    PerformanceReportService performanceReportService;
    @Resource
    PerformanceTestService performanceTestService;

    @GetMapping("/issues/plan/get/{shareId}/{planId}")
    public List<IssuesDao> getIssuesByPlanoId(@PathVariable String shareId, @PathVariable String planId) {
        shareInfoService.validate(shareId, planId);
        return issuesService.getIssuesByPlanoId(planId);
    }

    @GetMapping("/test/plan/report/{shareId}/{planId}")
    public TestPlanSimpleReportDTO getReport(@PathVariable String shareId, @PathVariable String planId) {
        shareInfoService.validate(shareId, planId);
        return testPlanService.getReport(planId);
    }

    @GetMapping("/report/export/{shareId}/{planId}")
    public void exportHtmlReport(@PathVariable String shareId, @PathVariable String planId, HttpServletResponse response) throws UnsupportedEncodingException {
        shareInfoService.validate(shareId, planId);
        testPlanService.exportPlanReport(planId, response);
    }

    @GetMapping("/test/plan/case/list/failure/{shareId}/{planId}")
    public List<TestPlanCaseDTO> getFailureCases(@PathVariable String shareId, @PathVariable String planId) {
        shareInfoService.validate(shareId, planId);
        return testPlanTestCaseService.getFailureCases(planId);
    }

    @GetMapping("/test/plan/case/list/all/{shareId}/{planId}")
    public List<TestPlanCaseDTO> getAllCases(@PathVariable String shareId, @PathVariable String planId) {
        shareInfoService.validate(shareId, planId);
        return testPlanTestCaseService.getAllCases(planId);
    }

    @GetMapping("/test/plan/load/case/list/failure/{shareId}/{planId}")
    public List<TestPlanLoadCaseDTO> getLoadFailureCases(@PathVariable String shareId, @PathVariable String planId) {
        shareInfoService.validate(shareId, planId);
        return testPlanLoadCaseService.getFailureCases(planId);
    }

    @GetMapping("/test/plan/load/case/list/all/{shareId}/{planId}")
    public List<TestPlanLoadCaseDTO> getLoadAllCases(@PathVariable String shareId, @PathVariable String planId) {
        shareInfoService.validate(shareId, planId);
        return testPlanLoadCaseService.getAllCases(planId);
    }

    @GetMapping("/test/plan/api/case/list/failure/{shareId}/{planId}")
    public List<TestPlanFailureApiDTO> getApiFailureList(@PathVariable String shareId, @PathVariable String planId) {
        shareInfoService.validate(shareId, planId);
        return testPlanApiCaseService.getFailureCases(planId);
    }

    @GetMapping("/test/plan/api/case/list/all/{shareId}/{planId}")
    public List<TestPlanFailureApiDTO> getApiAllList(@PathVariable String shareId, @PathVariable String planId) {
        shareInfoService.validate(shareId, planId);
        return testPlanApiCaseService.getAllCases(planId);
    }

    @GetMapping("/test/plan/scenario/case/list/failure/{shareId}/{planId}")
    public List<TestPlanFailureScenarioDTO> getScenarioFailureList(@PathVariable String shareId, @PathVariable String planId) {
        shareInfoService.validate(shareId, planId);
        return testPlanScenarioCaseService.getFailureCases(planId);
    }

    @GetMapping("/test/plan/scenario/case/list/all/{shareId}/{planId}")
    public List<TestPlanFailureScenarioDTO> getScenarioAllList(@PathVariable String shareId, @PathVariable String planId) {
        shareInfoService.validate(shareId, planId);
        return testPlanScenarioCaseService.getAllCases(planId);
    }

    @GetMapping("/api/definition/report/getReport/{shareId}/{testId}")
    public APIReportResult getApiReport(@PathVariable String shareId, @PathVariable String testId) {
        shareInfoService.apiReportValidate(shareId, testId);
        return apiDefinitionService.getDbResult(testId);
    }

    @GetMapping("/api/scenario/report/get/{shareId}/{reportId}")
    public APIScenarioReportResult get(@PathVariable String shareId, @PathVariable String reportId) {
        shareInfoService.scenarioReportValidate(shareId, reportId);
        return apiScenarioReportService.get(reportId);
    }

    @GetMapping("/performance/report/{shareId}/{reportId}")
    public LoadTestReportWithBLOBs getLoadTestReport(@PathVariable String shareId, @PathVariable String reportId) {
        //todo
        return performanceReportService.getLoadTestReport(reportId);
    }

    @GetMapping("/performance/report/content/report_time/{shareId}/{reportId}")
    public ReportTimeInfo getReportTimeInfo(@PathVariable String shareId, @PathVariable String reportId) {
        // todo
        return performanceReportService.getReportTimeInfo(reportId);
    }

    @PostMapping("/test/plan/load/case/report/exist/{shareId}")
    public Boolean isExistReport(@PathVariable String shareId, @RequestBody LoadCaseReportRequest request) {
        // testPlanLoadCaseService  todo checkout
        return testPlanLoadCaseService.isExistReport(request);
    }

    @GetMapping("/performance/report/get-jmx-content/{reportId}")
    public LoadTestExportJmx getJmxContent(@PathVariable String reportId) {
        return performanceReportService.getJmxContent(reportId);
    }

    @GetMapping("/performance/get-jmx-content/{testId}")
    public List<LoadTestExportJmx> getOldJmxContent(@PathVariable String testId) {
//        checkPermissionService.checkPerformanceTestOwner(testId);
        return performanceTestService.getJmxContent(testId);
    }

}
