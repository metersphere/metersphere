package io.metersphere.api.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.APIReportResult;
import io.metersphere.api.dto.DeleteAPIReportRequest;
import io.metersphere.api.dto.QueryAPIReportRequest;
import io.metersphere.api.service.APIReportService;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.DashboardTestDTO;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/api/report")
@RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
public class APIReportController {

    @Resource
    private APIReportService apiReportService;

    @GetMapping("recent/{count}")
    public List<APIReportResult> recentTest(@PathVariable int count) {
        String currentWorkspaceId = SessionUtils.getCurrentWorkspaceId();
        QueryAPIReportRequest request = new QueryAPIReportRequest();
        request.setWorkspaceId(currentWorkspaceId);
        PageHelper.startPage(1, count, true);
        return apiReportService.recentTest(request);
    }

    @GetMapping("/list/{testId}")
    public List<APIReportResult> listByTestId(@PathVariable String testId) {
        return apiReportService.listByTestId(testId);
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<APIReportResult>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryAPIReportRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        return PageUtils.setPageInfo(page, apiReportService.list(request));
    }

    @GetMapping("/get/{reportId}")
    public APIReportResult get(@PathVariable String reportId) {
        return apiReportService.get(reportId);
    }

    @PostMapping("/delete")
    public void delete(@RequestBody DeleteAPIReportRequest request) {
        apiReportService.delete(request);
    }

    @GetMapping("dashboard/tests")
    public List<DashboardTestDTO> dashboardTests() {
        return apiReportService.dashboardTests(SessionUtils.getCurrentWorkspaceId());
    }

    @PostMapping("/batch/delete")
    public void deleteAPIReportBatch(@RequestBody DeleteAPIReportRequest reportRequest) {
        apiReportService.deleteAPIReportBatch(reportRequest);
    }


}
