package io.metersphere.controller;

import io.metersphere.dto.IssuesDao;
import io.metersphere.dto.TestPlanCaseDTO;
import io.metersphere.plan.dto.TestPlanSimpleReportDTO;
import io.metersphere.plan.service.TestPlanReportService;
import io.metersphere.plan.service.TestPlanService;
import io.metersphere.plan.service.TestPlanTestCaseService;
import io.metersphere.service.BaseShareInfoService;
import io.metersphere.service.IssuesService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.List;

@RestController
@RequestMapping("share")
public class ShareController {

    @Resource
    BaseShareInfoService baseShareInfoService;
    @Resource
    IssuesService issuesService;
    @Resource
    TestPlanService testPlanService;
    @Resource
    TestPlanTestCaseService testPlanTestCaseService;
    @Resource
    TestPlanReportService testPlanReportService;

    @GetMapping("/issues/plan/get/{shareId}/{planId}")
    public List<IssuesDao> getIssuesByPlanoId(@PathVariable String shareId, @PathVariable String planId) {
        baseShareInfoService.validate(shareId, planId);
        return issuesService.getIssuesByPlanId(planId);
    }

    @GetMapping("/test/plan/report/{shareId}/{planId}")
    public TestPlanSimpleReportDTO getReport(@PathVariable String shareId, @PathVariable String planId) {
        baseShareInfoService.validate(shareId, planId);
        return testPlanService.getReport(planId, null);
    }

    @GetMapping("/report/export/{shareId}/{planId}/{lang}")
    public void exportHtmlReport(@PathVariable String shareId, @PathVariable String planId,
                                 @PathVariable(required = false) String lang, HttpServletResponse response) throws UnsupportedEncodingException {
        baseShareInfoService.validate(shareId, planId);
        testPlanService.exportPlanReport(planId, lang, response);
    }

    @PostMapping("/test/plan/case/list/all/{shareId}/{planId}")
    public List<TestPlanCaseDTO> getAllCases(@PathVariable String shareId, @PathVariable String planId,
                                             @RequestBody(required = false) List<String> statusList) {
        baseShareInfoService.validate(shareId, planId);
        return testPlanTestCaseService.getAllCasesByStatusList(planId, statusList);
    }

    @GetMapping("/test/plan/report/db/{shareId}/{reportId}")
    public TestPlanSimpleReportDTO getTestPlanDbReport(@PathVariable String shareId, @PathVariable String reportId) {
        baseShareInfoService.validate(shareId, reportId);
        return testPlanReportService.getReport(reportId);
    }
}
