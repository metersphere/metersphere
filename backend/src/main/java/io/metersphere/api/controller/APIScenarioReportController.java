package io.metersphere.api.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.APIReportBatchRequest;
import io.metersphere.api.dto.DeleteAPIReportRequest;
import io.metersphere.api.dto.QueryAPIReportRequest;
import io.metersphere.api.dto.automation.APIScenarioReportResult;
import io.metersphere.api.dto.automation.ExecuteType;
import io.metersphere.api.service.ApiScenarioReportService;
import io.metersphere.base.domain.ApiScenarioReport;
import io.metersphere.base.mapper.ext.ExtApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioReportMapper;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.notice.annotation.SendNotice;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/api/scenario/report")
public class APIScenarioReportController {

    @Resource
    private ApiScenarioReportService apiReportService;
    @Resource
    private ExtApiScenarioReportMapper extApiScenarioReportMapper;
    @Resource
    private ExtApiDefinitionExecResultMapper extApiDefinitionExecResultMapper;

    @GetMapping("/get/{reportId}")
    public APIScenarioReportResult get(@PathVariable String reportId) {
        return apiReportService.get(reportId);
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<APIScenarioReportResult>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryAPIReportRequest request) {
        Page<Object> page = new Page(goPage, pageSize*2, true);
        long scenarioCount = extApiScenarioReportMapper.selectCountByRequest(request);
        long definitionCount = extApiDefinitionExecResultMapper.selectCountByRequest(request);
        long totalCount = scenarioCount + definitionCount;
        page.setTotal(totalCount);
        return PageUtils.setPageInfo(page, apiReportService.list(goPage,pageSize*2,request));
    }

    @PostMapping("/update")
    public String update(@RequestBody APIScenarioReportResult node) {
        node.setExecuteType(ExecuteType.Saved.name());
        return apiReportService.update(node);
    }

    @PostMapping("/delete")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION_REPORT, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#request.id)", msClass = ApiScenarioReportService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.API_REPORT_TASK, event = NoticeConstants.Event.DELETE, target = "#targetClass.get(#request.id)", targetClass = ApiScenarioReportService.class,
            mailTemplate = "api/ReportDelete", subject = "接口报告通知")
    public void delete(@RequestBody DeleteAPIReportRequest request) {
        apiReportService.delete(request);
    }

    @PostMapping("/batch/delete")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION_REPORT, type = OperLogConstants.BATCH_DEL, beforeEvent = "#msClass.getLogDetails(#reportRequest.ids)", msClass = ApiScenarioReportService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.API_REPORT_TASK, event = NoticeConstants.Event.DELETE, target = "#targetClass.getByIds(#request.ids)", targetClass = ApiScenarioReportService.class,
            mailTemplate = "api/ReportDelete", subject = "接口报告通知")
    public void deleteAPIReportBatch(@RequestBody APIReportBatchRequest request) {
        apiReportService.deleteAPIReportBatch(request);
    }

    @PostMapping("/reName")
    public void reName(@RequestBody ApiScenarioReport reportRequest) {
        apiReportService.reName(reportRequest);
    }
}
