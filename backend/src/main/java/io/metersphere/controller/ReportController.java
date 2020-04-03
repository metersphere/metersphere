package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.LoadTestReport;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.controller.request.ReportRequest;
import io.metersphere.dto.ReportDTO;
import io.metersphere.report.base.ChartsData;
import io.metersphere.report.base.Errors;
import io.metersphere.report.base.TestOverview;
import io.metersphere.report.dto.ErrorsTop5DTO;
import io.metersphere.report.dto.RequestStatisticsDTO;
import io.metersphere.service.ReportService;
import io.metersphere.user.SessionUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "report")
public class ReportController {

    @Resource
    private ReportService reportService;

    @GetMapping("/recent/{count}")
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
    public List<LoadTestReport> recentProjects(@PathVariable int count) {
        String currentWorkspaceId = SessionUtils.getCurrentWorkspaceId();
        ReportRequest request = new ReportRequest();
        request.setWorkspaceId(currentWorkspaceId);
        // 最近 `count` 个项目
        PageHelper.startPage(1, count);
        return reportService.getRecentReportList(request);
    }

    @PostMapping("/list/all/{goPage}/{pageSize}")
    public Pager<List<ReportDTO>> getReportList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ReportRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, reportService.getReportList(request));
    }

    @PostMapping("/delete/{reportId}")
    public void deleteReport(@PathVariable String reportId) {
        reportService.deleteReport(reportId);
    }


    @GetMapping("/test/pro/info/{reportId}")
    public ReportDTO getReportTestAndProInfo(@PathVariable String reportId) {
        return reportService.getReportTestAndProInfo(reportId);
    }

    @GetMapping("/content/{reportId}")
    public RequestStatisticsDTO getReportContent(@PathVariable String reportId) {
        return reportService.getReport(reportId);
    }

    @GetMapping("/content/errors/{reportId}")
    public List<Errors> getReportErrors(@PathVariable String reportId) {
        return reportService.getReportErrors(reportId);
    }

    @GetMapping("/content/errors_top5/{reportId}")
    public ErrorsTop5DTO getReportErrorsTop5(@PathVariable String reportId) {
        return reportService.getReportErrorsTOP5(reportId);
    }

    @GetMapping("/content/testoverview/{reportId}")
    public TestOverview getTestOverview(@PathVariable String reportId) {
        return reportService.getTestOverview(reportId);
    }

    @GetMapping("/content/load_chart/{reportId}")
    public ChartsData getLoadChartData(@PathVariable String reportId) {
        return reportService.getLoadChartData(reportId);
    }

    @GetMapping("/content/res_chart/{reportId}")
    public ChartsData getResponseTimeChartData(@PathVariable String reportId) {
        return reportService.getResponseTimeChartData(reportId);
    }

    @GetMapping("/{reportId}")
    public LoadTestReport getLoadTestReport(@PathVariable String reportId) {
        return reportService.getLoadTestReport(reportId);
    }

}
