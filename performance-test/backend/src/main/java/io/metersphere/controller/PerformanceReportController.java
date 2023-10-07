package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.LoadTestReportLog;
import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.dto.*;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.notice.annotation.SendNotice;
import io.metersphere.request.DeleteReportRequest;
import io.metersphere.request.RenameReportRequest;
import io.metersphere.request.ReportRequest;
import io.metersphere.service.PerformanceReportService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "performance/report")
public class PerformanceReportController {

    @Resource
    private PerformanceReportService performanceReportService;

    @PostMapping("/recent/{count}")
    @RequiresPermissions(value = {"PROJECT_PERFORMANCE_REPORT:READ", "PROJECT_PERFORMANCE_HOME:READ"}, logical = Logical.OR)
    public List<ReportDTO> recentProjects(@PathVariable int count, @RequestBody ReportRequest request) {
        // 最近 `count` 个项目
        PageHelper.startPage(1, count);
        return performanceReportService.getRecentReportList(request);
    }

    @PostMapping("/list/all/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_REPORT_READ)
    public Pager<List<ReportDTO>> getReportList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ReportRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, performanceReportService.getReportList(request));
    }

    @PostMapping("/delete/{reportId}")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_REPORT_READ_DELETE)
    @MsAuditLog(module = OperLogModule.PERFORMANCE_TEST_REPORT, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#reportId)", msClass = PerformanceReportService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.PERFORMANCE_REPORT_TASK, event = NoticeConstants.Event.DELETE,
            target = "#targetClass.getReport(#reportId)", targetClass = PerformanceReportService.class, subject = "性能测试报告通知")
    public void deleteReport(@PathVariable String reportId) {
        performanceReportService.deleteReport(reportId);
    }


    @GetMapping("/test/pro/info/{reportId}")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_REPORT_READ)
    public ReportDTO getReportTestAndProInfo(@PathVariable String reportId) {
        return performanceReportService.getReportTestAndProInfo(reportId);
    }

    @GetMapping("/content/{reportId}")
    public List<Statistics> getReportContent(@PathVariable String reportId) {
        return performanceReportService.getReportStatistics(reportId);
    }

    @GetMapping("/content/errors/{reportId}")
    public List<Errors> getReportErrors(@PathVariable String reportId) {
        return performanceReportService.getReportErrors(reportId);
    }

    @GetMapping("/content/{reportKey}/{reportId}")
    public List<ChartsData> getReportChart(@PathVariable String reportKey, @PathVariable String reportId) {
        return performanceReportService.getReportChart(reportKey, reportId);
    }

    @GetMapping("/content/errors_top5/{reportId}")
    public List<ErrorsTop5> getReportErrorsTop5(@PathVariable String reportId) {
        return performanceReportService.getReportErrorsTOP5(reportId);
    }

    @GetMapping("/content/errors_samples/{reportId}")
    public SamplesRecord getErrorSamples(@PathVariable String reportId) {
        return performanceReportService.getErrorSamples(reportId);
    }

    @GetMapping("/content/testoverview/{reportId}")
    public TestOverview getTestOverview(@PathVariable String reportId) {
        return performanceReportService.getTestOverview(reportId);
    }

    @GetMapping("/content/report_time/{reportId}")
    public ReportTimeInfo getReportTimeInfo(@PathVariable String reportId) {
        return performanceReportService.getReportTimeInfo(reportId);
    }

    @GetMapping("/content/load_chart/{reportId}")
    public List<ChartsData> getLoadChartData(@PathVariable String reportId) {
        return performanceReportService.getLoadChartData(reportId);
    }

    @GetMapping("/content/res_chart/{reportId}")
    public List<ChartsData> getResponseTimeChartData(@PathVariable String reportId) {
        return performanceReportService.getResponseTimeChartData(reportId);
    }

    @GetMapping("/content/error_chart/{reportId}")
    public List<ChartsData> getErrorChartData(@PathVariable String reportId) {
        return performanceReportService.getErrorChartData(reportId);
    }

    @GetMapping("/content/response_code_chart/{reportId}")
    public List<ChartsData> getResponseCodeChartData(@PathVariable String reportId) {
        return performanceReportService.getResponseCodeChartData(reportId);
    }

    @GetMapping("/{reportId}")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_REPORT_READ)
    public LoadTestReportWithBLOBs getLoadTestReport(@PathVariable String reportId) {
        return performanceReportService.getLoadTestReport(reportId);
    }

    @GetMapping("log/resource/{reportId}")
    public List<LogDetailDTO> getResourceIds(@PathVariable String reportId) {
        return performanceReportService.getReportLogResource(reportId);
    }

    @GetMapping("log/{reportId}/{resourceId}/{goPage}")
    public Pager<List<LoadTestReportLog>> logs(@PathVariable String reportId, @PathVariable String resourceId, @PathVariable int goPage) {
        Page<Object> page = PageHelper.startPage(goPage, 5, true);
        return PageUtils.setPageInfo(page, performanceReportService.getReportLogs(reportId, resourceId));
    }

    @GetMapping("log/download/{reportId}/{resourceId}")
    public void downloadLog(@PathVariable String reportId, @PathVariable String resourceId, HttpServletResponse response) throws Exception {
        performanceReportService.downloadLog(response, reportId, resourceId);
    }

    @PostMapping("/batch/delete")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_REPORT_READ_DELETE)
    @MsAuditLog(module = OperLogModule.PERFORMANCE_TEST_REPORT, type = OperLogConstants.BATCH_DEL, beforeEvent = "#msClass.getLogDetails(#reportRequest.ids)", msClass = PerformanceReportService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.PERFORMANCE_REPORT_TASK, event = NoticeConstants.Event.DELETE,
            target = "#targetClass.getReportList(#reportRequest.ids)", targetClass = PerformanceReportService.class, subject = "性能测试报告通知")
    public void deleteReportBatch(@RequestBody DeleteReportRequest reportRequest) {
        performanceReportService.deleteReportBatch(reportRequest);
    }

    @GetMapping("get-jmx-content/{reportId}")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_REPORT_READ)
    public List<LoadTestExportJmx> getJmxContent(@PathVariable String reportId) {
        return performanceReportService.getJmxContent(reportId);
    }

    @GetMapping("/get-load-config/{reportId}")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_REPORT_READ)
    public String getLoadConfiguration(@PathVariable String reportId) {
        return performanceReportService.getLoadConfiguration(reportId);
    }

    @GetMapping("/get-advanced-config/{reportId}")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_REPORT_READ)
    public String getAdvancedConfiguration(@PathVariable String reportId) {
        return performanceReportService.getAdvancedConfiguration(reportId);
    }

    @PostMapping("rename")
    @MsAuditLog(module = OperLogModule.PERFORMANCE_TEST_REPORT, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request.id)", title = "#request.name", content = "#msClass.getLogDetails(#request.id)", msClass = PerformanceReportService.class)
    public void renameReport(@RequestBody RenameReportRequest request) {
        performanceReportService.renameReport(request);
    }

    @PostMapping("/plan/report")
    public List<String> selectForPlanReport(@RequestBody List<String> apiReportIds) {
        return performanceReportService.selectForPlanReport(apiReportIds);
    }
}
