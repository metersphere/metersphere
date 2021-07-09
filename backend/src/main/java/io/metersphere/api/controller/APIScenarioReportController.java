package io.metersphere.api.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.APIReportBatchRequest;
import io.metersphere.api.dto.DeleteAPIReportRequest;
import io.metersphere.api.dto.QueryAPIReportRequest;
import io.metersphere.api.dto.automation.APIScenarioReportResult;
import io.metersphere.api.dto.automation.ExecuteType;
import io.metersphere.api.jmeter.TestResult;
import io.metersphere.api.service.ApiScenarioReportService;
import io.metersphere.api.service.MsResultService;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.log.annotation.MsAuditLog;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/api/scenario/report")
public class APIScenarioReportController {

    @Resource
    private ApiScenarioReportService apiReportService;
    @Resource
    private MsResultService resultService;

    @GetMapping("/get/{reportId}")
    public APIScenarioReportResult get(@PathVariable String reportId) {
        return apiReportService.get(reportId);
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<APIScenarioReportResult>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryAPIReportRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, apiReportService.list(request));
    }

    @PostMapping("/update")
    public String update(@RequestBody APIScenarioReportResult node) {
        node.setExecuteType(ExecuteType.Saved.name());
        return apiReportService.update(node);
    }

    @PostMapping("/delete")
    @MsAuditLog(module = "api_automation_report", type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#request.id)", msClass = ApiScenarioReportService.class)
    public void delete(@RequestBody DeleteAPIReportRequest request) {
        apiReportService.delete(request);
    }

    @PostMapping("/batch/delete")
    @MsAuditLog(module = "api_automation_report", type = OperLogConstants.BATCH_DEL, beforeEvent = "#msClass.getLogDetails(#reportRequest.ids)", msClass = ApiScenarioReportService.class)
    public void deleteAPIReportBatch(@RequestBody APIReportBatchRequest reportRequest) {
        apiReportService.deleteAPIReportBatch(reportRequest);
    }

    @GetMapping("/get/real/{reportId}")
    public TestResult getRealReport(@PathVariable String reportId) {
        return resultService.getResult(reportId);
    }

    @GetMapping("/remove/real/{reportId}")
    public void removeRealReport(@PathVariable String reportId) {
        resultService.delete(reportId);
    }

}
