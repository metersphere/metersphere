package io.metersphere.track.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.TestPlanReport;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.track.dto.TestPlanReportDTO;
import io.metersphere.track.request.report.QueryTestPlanReportRequest;
import io.metersphere.track.request.report.TestPlanReportSaveRequest;
import io.metersphere.track.service.TestPlanReportService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

/**
 * @author song.tianyang
 * @Date 2021/1/8 2:38 下午
 * @Description
 */
@RequestMapping("/test/plan/report")
@RestController
public class TestPlanReportController {

    @Resource
    private TestPlanReportService testPlanReportService;
    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<TestPlanReportDTO>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestPlanReportRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testPlanReportService.list(request));
    }
    @GetMapping("/getMetric/{planId}")
    public TestPlanReportDTO getMetric(@PathVariable String planId) {
        return testPlanReportService.getMetric(planId);
    }

    @GetMapping("/sendTask/{planId}")
    public String sendTask(@PathVariable String planId) {
        TestPlanReport report = testPlanReportService.getTestPlanReport(planId);
        testPlanReportService.update(report);
        return "sucess";
    }

    @GetMapping("/status/{planId}")
    public String getStatus(@PathVariable String planId) {
        TestPlanReport report = testPlanReportService.getTestPlanReport(planId);
        String status = report.getStatus();
        return status;
    }

    @PostMapping("/delete")
    @MsAuditLog(module = "track_report", type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#testPlanReportIdList)", msClass = TestPlanReportService.class)
    public void delete(@RequestBody List<String> testPlanReportIdList) {
        testPlanReportService.delete(testPlanReportIdList);
    }

    @PostMapping("/deleteBatchByParams")
    public void deleteBatchByParams(@RequestBody QueryTestPlanReportRequest request) {
        testPlanReportService.delete(request);
    }


    @GetMapping("/apiExecuteFinish/{planId}/{userId}")
    public void apiExecuteFinish(@PathVariable String planId,@PathVariable String userId) {
        String reportId = UUID.randomUUID().toString();
        TestPlanReportSaveRequest saveRequest = new TestPlanReportSaveRequest(reportId,planId,userId,ReportTriggerMode.API.name());
        TestPlanReport report = testPlanReportService.genTestPlanReport(saveRequest);
        testPlanReportService.countReportByTestPlanReportId(report.getId(),null, ReportTriggerMode.API.name(),null);
    }

    @GetMapping("/saveTestPlanReport/{planId}/{triggerMode}")
    public String saveTestPlanReport(@PathVariable String planId,@PathVariable String triggerMode) {
        String userId = SessionUtils.getUser().getId();
        String reportId = UUID.randomUUID().toString();
        TestPlanReportSaveRequest saveRequest = new TestPlanReportSaveRequest(reportId,planId,userId,triggerMode);
        TestPlanReport report = testPlanReportService.genTestPlanReport(saveRequest);
        testPlanReportService.countReportByTestPlanReportId(report.getId(),null, triggerMode,null);
        return "success";
    }
}
