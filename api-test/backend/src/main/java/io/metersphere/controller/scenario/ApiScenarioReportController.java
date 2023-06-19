package io.metersphere.controller.scenario;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.ApiReportBatchRequest;
import io.metersphere.api.dto.DeleteAPIReportRequest;
import io.metersphere.api.dto.QueryAPIReportRequest;
import io.metersphere.api.dto.automation.ApiScenarioReportResult;
import io.metersphere.api.dto.automation.ExecuteType;
import io.metersphere.base.domain.ApiScenarioReport;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.dto.PlanReportCaseDTO;
import io.metersphere.dto.RequestResult;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.notice.annotation.SendNotice;
import io.metersphere.service.ShareInfoService;
import io.metersphere.service.scenario.ApiScenarioReportService;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/scenario/report")
public class ApiScenarioReportController {

    @Resource
    private ApiScenarioReportService apiReportService;
    @Resource
    private ShareInfoService shareInfoService;

    @GetMapping("/get/{reportId}")
    public ApiScenarioReportResult get(@PathVariable String reportId) {
        return apiReportService.get(reportId, false);
    }

    @GetMapping("/get/{shareId}/{reportId}")
    public ApiScenarioReportResult get(@PathVariable String shareId, @PathVariable String reportId) {
        shareInfoService.validateExpired(shareId);
        return apiReportService.get(reportId, false);
    }

    @GetMapping("/get/detail/{reportId}")
    public ApiScenarioReportResult getAll(@PathVariable String reportId) {
        return apiReportService.get(reportId, true);
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_API_REPORT_READ)
    public Pager<List<ApiScenarioReportResult>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryAPIReportRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, apiReportService.list(request));
    }

    @PostMapping("/update")
    public String update(@RequestBody ApiScenarioReportResult node) {
        node.setExecuteType(ExecuteType.Saved.name());
        return apiReportService.update(node);
    }

    @GetMapping("/get/step/detail/{stepId}")
    public RequestResult selectReportContent(@PathVariable String stepId) {
        return apiReportService.selectReportContent(stepId);
    }

    @PostMapping("/delete")
    @RequiresPermissions(PermissionConstants.PROJECT_API_REPORT_READ_DELETE)
    @MsAuditLog(module = OperLogModule.API_AUTOMATION_REPORT, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#request.id)", msClass = ApiScenarioReportService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.API_REPORT_TASK, event = NoticeConstants.Event.DELETE, target = "#targetClass.get(#request.id, false)", targetClass = ApiScenarioReportService.class,
            subject = "接口报告通知")
    public void delete(@RequestBody DeleteAPIReportRequest request) {
        apiReportService.delete(request);
    }

    @PostMapping("/batch/delete")
    @RequiresPermissions(PermissionConstants.PROJECT_API_REPORT_READ_DELETE)
    @MsAuditLog(module = OperLogModule.API_AUTOMATION_REPORT, type = OperLogConstants.BATCH_DEL, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = ApiScenarioReportService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.API_REPORT_TASK, event = NoticeConstants.Event.DELETE, target = "#targetClass.getByIds(#request.ids)", targetClass = ApiScenarioReportService.class,
            subject = "接口报告通知")
    public void deleteAPIReportBatch(@RequestBody ApiReportBatchRequest request) {
        apiReportService.deleteAPIReportBatch(request);
    }

    @PostMapping("/rename")
    public void reName(@RequestBody ApiScenarioReport reportRequest) {
        apiReportService.reName(reportRequest);
    }

    @PostMapping("/plan/report")
    public List<PlanReportCaseDTO> selectForPlanReport(@RequestBody List<String> reportIds) {
        return apiReportService.selectForPlanReport(reportIds);
    }

    @PostMapping("/plan/status/map")
    public Map<String, String> selectReportResultByReportIds(@RequestBody List<String> apiReportIds) {
        return apiReportService.getReportStatusByReportIds(apiReportIds);
    }
}
