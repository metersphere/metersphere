package io.metersphere.performance.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.LoadTestReportLog;
import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.dto.LogDetailDTO;
import io.metersphere.dto.ReportDTO;
import io.metersphere.performance.base.*;
import io.metersphere.performance.controller.request.DeleteReportRequest;
import io.metersphere.performance.controller.request.RenameReportRequest;
import io.metersphere.performance.controller.request.ReportRequest;
import io.metersphere.performance.dto.LoadTestExportJmx;
import io.metersphere.performance.service.PerformanceReportService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping(value = "performance/report")
public class PerformanceReportController {

    @Resource
    private PerformanceReportService performanceReportService;

    @PostMapping("/recent/{count}")
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
    public List<ReportDTO> recentProjects(@PathVariable int count, @RequestBody ReportRequest request) {
        // 最近 `count` 个项目
        PageHelper.startPage(1, count);
        return performanceReportService.getRecentReportList(request);
    }

    @PostMapping("/list/all/{goPage}/{pageSize}")
    public Pager<List<ReportDTO>> getReportList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ReportRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, performanceReportService.getReportList(request));
    }

    @PostMapping("/delete/{reportId}")
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER}, logical = Logical.OR)
    public void deleteReport(@PathVariable String reportId) {
        performanceReportService.deleteReport(reportId);
    }


    @GetMapping("/test/pro/info/{reportId}")
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

    @GetMapping("/content/errors_top5/{reportId}")
    public List<ErrorsTop5> getReportErrorsTop5(@PathVariable String reportId) {
        return performanceReportService.getReportErrorsTOP5(reportId);
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
    public LoadTestReportWithBLOBs getLoadTestReport(@PathVariable String reportId) {
        return performanceReportService.getLoadTestReport(reportId);
    }

    @GetMapping("/pool/type/{reportId}")
    public String getPoolTypeByReportId(@PathVariable String reportId) {
        return performanceReportService.getPoolTypeByReportId(reportId);
    }

    @GetMapping("log/resource/{reportId}")
    public List<LogDetailDTO> getResourceIds(@PathVariable String reportId) {
        return performanceReportService.getReportLogResource(reportId);
    }

    @GetMapping("log/{reportId}/{resourceId}/{goPage}")
    public Pager<List<LoadTestReportLog>> logs(@PathVariable String reportId, @PathVariable String resourceId, @PathVariable int goPage) {
        Page<Object> page = PageHelper.startPage(goPage, 1, true);
        return PageUtils.setPageInfo(page, performanceReportService.getReportLogs(reportId, resourceId));
    }

    @GetMapping("log/download/{reportId}/{resourceId}")
    public void downloadLog(@PathVariable String reportId, @PathVariable String resourceId, HttpServletResponse response) throws Exception {
        performanceReportService.downloadLog(response, reportId, resourceId);
    }

    @PostMapping("/batch/delete")
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER}, logical = Logical.OR)
    public void deleteReportBatch(@RequestBody DeleteReportRequest reportRequest) {
        performanceReportService.deleteReportBatch(reportRequest);
    }

    @GetMapping("/jtl/download/{reportId}")
    public void downloadJtlZip(@PathVariable String reportId, HttpServletResponse response) {
        performanceReportService.downloadJtlZip(reportId, response);
    }

    @GetMapping("get-jmx-content/{reportId}")
    public LoadTestExportJmx getJmxContent(@PathVariable String reportId) {
        return performanceReportService.getJmxContent(reportId);
    }

    @PostMapping("rename")
    public void renameReport(@RequestBody RenameReportRequest request) {
        performanceReportService.renameReport(request);
    }
}
