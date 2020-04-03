package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.ApiTestReport;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.controller.request.ReportRequest;
import io.metersphere.dto.ApiReportDTO;
import io.metersphere.service.ApiReportService;
import io.metersphere.user.SessionUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/api/report")
public class ApiReportController {

    @Resource
    private ApiReportService apiReportService;

    @GetMapping("/recent/{count}")
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
    public List<ApiTestReport> recentProjects(@PathVariable int count) {
        String currentWorkspaceId = SessionUtils.getCurrentWorkspaceId();
        ReportRequest request = new ReportRequest();
        request.setWorkspaceId(currentWorkspaceId);
        PageHelper.startPage(1, count);
        return apiReportService.getRecentReportList(request);
    }

    @PostMapping("/list/all/{goPage}/{pageSize}")
    public Pager<List<ApiReportDTO>> getReportList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ReportRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, apiReportService.getReportList(request));
    }

    @PostMapping("/delete/{reportId}")
    public void deleteReport(@PathVariable String reportId) {
        apiReportService.deleteReport(reportId);
    }

    @GetMapping("/test/pro/info/{reportId}")
    public ApiReportDTO getReportTestAndProInfo(@PathVariable String reportId) {
        return apiReportService.getReportTestAndProInfo(reportId);
    }

}
