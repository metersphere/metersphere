package io.metersphere.controller;

import io.metersphere.api.dto.APIReportResult;
import io.metersphere.api.dto.automation.APIScenarioReportResult;
import io.metersphere.api.dto.automation.TestPlanFailureApiDTO;
import io.metersphere.api.dto.automation.TestPlanFailureScenarioDTO;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.api.service.ApiScenarioReportService;
import io.metersphere.base.domain.IssuesDao;
import io.metersphere.track.dto.TestPlanCaseDTO;
import io.metersphere.track.dto.TestPlanLoadCaseDTO;
import io.metersphere.track.dto.TestPlanSimpleReportDTO;
import io.metersphere.track.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Controller
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

    // Todo 鉴权

    @ResponseBody
    @GetMapping("/issues/plan/get/{planId}")
    public List<IssuesDao> getIssuesByPlanoId(@PathVariable String planId) {
        return issuesService.getIssuesByPlanoId(planId);
    }

    @ResponseBody
    @GetMapping("/test/plan/report/{planId}")
    public TestPlanSimpleReportDTO getReport(@PathVariable String planId) {
        return testPlanService.getReport(planId);
    }

    @ResponseBody
    @GetMapping("/report/export/{planId}")
    public void exportHtmlReport(@PathVariable String planId, HttpServletResponse response) throws UnsupportedEncodingException {
        testPlanService.exportPlanReport(planId, response);
    }

    @ResponseBody
    @GetMapping("/test/plan/case/list/failure/{planId}")
    public List<TestPlanCaseDTO> getFailureCases(@PathVariable String planId) {
        return testPlanTestCaseService.getFailureCases(planId);
    }

    @ResponseBody
    @GetMapping("/test/plan/load/case/list/failure/{planId}")
    public List<TestPlanLoadCaseDTO> getLoadFailureCases(@PathVariable String planId) {
        return testPlanLoadCaseService.getFailureCases(planId);
    }

    @ResponseBody
    @GetMapping("/test/plan/api/case/list/failure/{planId}")
    public List<TestPlanFailureApiDTO> getApiFailureList(@PathVariable String planId) {
        return testPlanApiCaseService.getFailureList(planId);
    }

    @ResponseBody
    @GetMapping("/test/plan/scenario/case/list/failure/{planId}")
    public List<TestPlanFailureScenarioDTO> getScenarioFailureList(@PathVariable String planId) {
        return testPlanScenarioCaseService.getFailureList(planId);
    }

    @ResponseBody
    @GetMapping("/api/definition/report/getReport/{testId}")
    public APIReportResult getApiReport(@PathVariable String testId) {
        return apiDefinitionService.getDbResult(testId);
    }

    @ResponseBody
    @GetMapping("/api/scenario/report/get/{reportId}")
    public APIScenarioReportResult get(@PathVariable String reportId) {
        return apiScenarioReportService.get(reportId);
    }
}
