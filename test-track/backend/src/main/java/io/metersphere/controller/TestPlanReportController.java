package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.TestPlanReport;
import io.metersphere.base.domain.TestPlanReportContentWithBLOBs;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.TestPlanReportDTO;
import io.metersphere.dto.TestPlanScheduleReportInfoDTO;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.log.annotation.MsRequestLog;
import io.metersphere.notice.annotation.SendNotice;
import io.metersphere.plan.dto.TestPlanReportDataStruct;
import io.metersphere.plan.request.TestPlanReportSaveRequest;
import io.metersphere.plan.service.TestPlanReportService;
import io.metersphere.request.report.QueryTestPlanReportRequest;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

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
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REPORT_READ)
    public Pager<List<TestPlanReportDTO>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestPlanReportRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testPlanReportService.list(request));
    }

    @GetMapping("/getMetric/{planId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REPORT_READ)
    public TestPlanReportDTO getMetric(@PathVariable String planId) {
        return testPlanReportService.getMetric(planId);
    }

    @GetMapping("/real-time/{planId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REPORT_READ)
    public TestPlanReportDataStruct getRealTimeReport(@PathVariable String planId) {
        return testPlanReportService.getRealTimeReport(planId);
    }

    @GetMapping("/db/{reportId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REPORT_READ)
    public TestPlanReportDataStruct getReport(@PathVariable String reportId) {
        return testPlanReportService.getReport(reportId);
    }

    @GetMapping("/status/{planId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REPORT_READ)
    public String getStatus(@PathVariable String planId) {
        TestPlanReport report = testPlanReportService.getTestPlanReport(planId);
        String status = report.getStatus();
        return status;
    }

    @PostMapping("/delete")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REPORT_READ_DELETE)
    @MsAuditLog(module = OperLogModule.TRACK_REPORT, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#testPlanReportIdList)", msClass = TestPlanReportService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.TRACK_REPORT_TASK, target = "#targetClass.getReports(#testPlanReportIdList)", targetClass = TestPlanReportService.class,
            event = NoticeConstants.Event.DELETE, subject = "报告通知")
    public void delete(@RequestBody List<String> testPlanReportIdList) {
        testPlanReportService.delete(testPlanReportIdList);
    }

    @PostMapping("/deleteBatchByParams")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REPORT_READ_DELETE)
    public void deleteBatchByParams(@RequestBody QueryTestPlanReportRequest request) {
        testPlanReportService.delete(request);
    }

    @GetMapping("/saveTestPlanReport/{planId}/{triggerMode}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REPORT_READ_EDIT)
    public String saveTestPlanReport(@PathVariable String planId, @PathVariable String triggerMode) {
        String userId = SessionUtils.getUser().getId();
        String reportId = UUID.randomUUID().toString();
        TestPlanReportSaveRequest saveRequest = new TestPlanReportSaveRequest(reportId, planId, userId, triggerMode);
        TestPlanScheduleReportInfoDTO report = testPlanReportService.genTestPlanReport(saveRequest, null);
        testPlanReportService.genTestPlanReportContent(report);
        testPlanReportService.countReportByTestPlanReportId(report.getTestPlanReport().getId(), null, triggerMode);
        return "success";
    }

    @PostMapping("/reName")
    @MsRequestLog(module = OperLogModule.TRACK_REPORT)
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REPORT_READ_EDIT)
    public void reName(@RequestBody TestPlanReport request) {
        testPlanReportService.reName(request.getId(), request.getName());
    }

    @PostMapping("/edit/report")
    @MsRequestLog(module = OperLogModule.TRACK_REPORT)
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REPORT_READ_EDIT)
    public void editReport(@RequestBody TestPlanReportContentWithBLOBs reportContentWithBLOBs) {
        testPlanReportService.editReport(reportContentWithBLOBs);
    }
}
