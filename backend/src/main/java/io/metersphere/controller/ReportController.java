package io.metersphere.controller;

import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.LoadTestReport;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.controller.request.ReportRequest;
import io.metersphere.service.ReportService;
import io.metersphere.user.SessionUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
