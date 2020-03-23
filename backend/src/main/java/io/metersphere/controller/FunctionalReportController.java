package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.FucTestLog;
import io.metersphere.base.domain.FucTestReport;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.controller.request.ReportRequest;
import io.metersphere.dto.FunctionalReportDTO;
import io.metersphere.service.FunctionalReportService;
import io.metersphere.user.SessionUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/functional/report")
public class FunctionalReportController {

    @Resource
    private FunctionalReportService functionalReportService;

    @GetMapping("/recent/{count}")
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
    public List<FucTestReport> recentProjects(@PathVariable int count) {
        String currentWorkspaceId = SessionUtils.getCurrentWorkspaceId();
        ReportRequest request = new ReportRequest();
        request.setWorkspaceId(currentWorkspaceId);
        PageHelper.startPage(1, count);
        return functionalReportService.getRecentReportList(request);
    }

    @PostMapping("/list/all/{goPage}/{pageSize}")
    public Pager<List<FunctionalReportDTO>> getReportList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ReportRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, functionalReportService.getReportList(request));
    }

    @PostMapping("/delete/{reportId}")
    public void deleteReport(@PathVariable String reportId) {
        functionalReportService.deleteReport(reportId);
    }

    @GetMapping("/test/pro/info/{reportId}")
    public FunctionalReportDTO getReportTestAndProInfo(@PathVariable String reportId) {
        return functionalReportService.getReportTestAndProInfo(reportId);
    }

}
