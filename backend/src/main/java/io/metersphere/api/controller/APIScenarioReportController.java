package io.metersphere.api.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.DeleteAPIReportRequest;
import io.metersphere.api.dto.QueryAPIReportRequest;
import io.metersphere.api.dto.automation.APIScenarioReportResult;
import io.metersphere.api.dto.automation.ExecuteType;
import io.metersphere.api.service.ApiScenarioReportService;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/api/scenario/report")
@RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
public class APIScenarioReportController {

    @Resource
    private ApiScenarioReportService apiReportService;

    @GetMapping("/get/{reportId}")
    public APIScenarioReportResult get(@PathVariable String reportId) {
        return apiReportService.get(reportId);
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<APIScenarioReportResult>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryAPIReportRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        return PageUtils.setPageInfo(page, apiReportService.list(request));
    }

    @PostMapping("/update")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public String update(@RequestBody APIScenarioReportResult node) {
        node.setExecuteType(ExecuteType.Saved.name());
        return apiReportService.update(node);
    }

    @PostMapping("/delete")
    public void delete(@RequestBody DeleteAPIReportRequest request) {
        apiReportService.delete(request);
    }

    @PostMapping("/batch/delete")
    public void deleteAPIReportBatch(@RequestBody DeleteAPIReportRequest reportRequest) {
        apiReportService.deleteAPIReportBatch(reportRequest);
    }

}
