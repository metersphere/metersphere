package io.metersphere.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.metersphere.commons.utils.HttpHeaderUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.TestPlanCaseDTO;
import io.metersphere.plan.dto.ExecutionModeDTO;
import io.metersphere.plan.dto.TestPlanReportDataStruct;
import io.metersphere.plan.service.TestPlanReportService;
import io.metersphere.plan.service.TestPlanService;
import io.metersphere.plan.service.TestPlanTestCaseService;
import io.metersphere.service.IssuesService;
import io.metersphere.service.ShareInfoService;
import io.metersphere.xpack.track.dto.IssuesDao;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.List;

@RestController
@RequestMapping("share")
public class ShareController {

    @Resource
    ShareInfoService shareInfoService;
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
        shareInfoService.validate(shareId, planId);
        return issuesService.getIssuesByPlanId(planId);
    }

    @GetMapping("/test/plan/report/{shareId}/{planId}")
    public TestPlanReportDataStruct getReport(@PathVariable String shareId, @PathVariable String planId) {
        shareInfoService.validate(shareId, planId);
        return testPlanService.getShareReport(shareInfoService.get(shareId), planId);
    }

    @GetMapping("/report/export/{shareId}/{planId}/{lang}")
    public void exportHtmlReport(@PathVariable String shareId, @PathVariable String planId,
                                 @PathVariable(required = false) String lang, HttpServletResponse response) throws UnsupportedEncodingException, JsonProcessingException {
        shareInfoService.validate(shareId, planId);
        testPlanService.exportPlanReport(planId, lang, response);
    }

    @PostMapping("/test/plan/case/list/all/{shareId}/{planId}")
    public List<TestPlanCaseDTO> getAllCases(@PathVariable String shareId, @PathVariable String planId,
                                             @RequestBody(required = false) List<String> statusList) {
        shareInfoService.validate(shareId, planId);
        return testPlanTestCaseService.getAllCasesByStatusList(planId, statusList);
    }

    @GetMapping("/test/plan/report/db/{shareId}/{reportId}")
    public TestPlanReportDataStruct getTestPlanDbReport(@PathVariable String shareId, @PathVariable String reportId) {
        shareInfoService.validate(shareId, reportId);
        return testPlanReportService.getShareDbReport(shareInfoService.get(shareId), reportId);
    }

    @GetMapping("test/plan/ext/report/{shareId}/{reportId}")
    public ExecutionModeDTO getExtReport(@PathVariable String shareId, @PathVariable String reportId) throws JsonProcessingException {
        shareInfoService.validate(shareId, reportId);
        if (SessionUtils.getUser() == null) {
            HttpHeaderUtils.runAsUser("admin");
        }
        //          testPlanService.getExtInfoByPlanId 这个方法逻辑有问题。分不清楚干嘛用的。方法删了，调用地方先注释了。
        //        TestPlanExtReportDTO reportExtInfo = testPlanService.getExtInfoByReportId(reportId);
        ExecutionModeDTO reportExtInfo = new ExecutionModeDTO();
        HttpHeaderUtils.clearUser();
        return reportExtInfo;
    }
}
