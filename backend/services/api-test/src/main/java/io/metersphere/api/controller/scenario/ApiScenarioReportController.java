package io.metersphere.api.controller.scenario;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.definition.ApiReportBatchRequest;
import io.metersphere.api.dto.definition.ApiReportPageRequest;
import io.metersphere.api.dto.definition.ApiTaskReportDTO;
import io.metersphere.api.dto.report.ApiScenarioReportListDTO;
import io.metersphere.api.dto.scenario.ApiScenarioReportDTO;
import io.metersphere.api.dto.scenario.ApiScenarioReportDetailDTO;
import io.metersphere.api.dto.scenario.ExecTaskDetailDTO;
import io.metersphere.api.service.ApiReportShareService;
import io.metersphere.api.service.scenario.ApiScenarioReportLogService;
import io.metersphere.api.service.scenario.ApiScenarioReportNoticeService;
import io.metersphere.api.service.scenario.ApiScenarioReportService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.domain.ShareInfo;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.notice.annotation.SendNotice;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/report/scenario")
@Tag(name = "接口测试-接口报告-场景")
public class ApiScenarioReportController {
    @Resource
    private ApiScenarioReportService apiScenarioReportService;
    @Resource
    private ApiReportShareService apiReportShareService;

    @PostMapping("/page")
    @Operation(summary = "接口测试-接口报告-场景()")
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    @RequiresPermissions(PermissionConstants.PROJECT_API_REPORT_READ)
    public Pager<List<ApiScenarioReportListDTO>> getPage(@Validated @RequestBody ApiReportPageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "start_time desc");
        return PageUtils.setPageInfo(page, apiScenarioReportService.getPage(request));
    }

    @PostMapping("/rename/{id}")
    @Operation(summary = "接口测试-接口报告-场景报告重命名")
    @CheckOwner(resourceId = "#id", resourceType = "api_scenario_report")
    @RequiresPermissions(PermissionConstants.PROJECT_API_REPORT_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#id)", msClass = ApiScenarioReportLogService.class)
    public void rename(@PathVariable String id, @RequestBody Object name) {
        apiScenarioReportService.rename(id, name.toString(), SessionUtils.getUserId());
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "接口测试-接口报告-场景报告删除")
    @CheckOwner(resourceId = "#id", resourceType = "api_scenario_report")
    @RequiresPermissions(PermissionConstants.PROJECT_API_REPORT_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = ApiScenarioReportLogService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.API_REPORT_TASK, event = NoticeConstants.Event.DELETE, target = "#targetClass.getDto(#id)", targetClass = ApiScenarioReportNoticeService.class)
    public void delete(@PathVariable String id) {
        apiScenarioReportService.delete(id, SessionUtils.getUserId());
    }

    @PostMapping("/batch/delete")
    @Operation(summary = "接口测试-接口报告-场景报告批量删除")
    @CheckOwner(resourceId = "#request.getSelectIds()", resourceType = "api_scenario_report")
    @RequiresPermissions(PermissionConstants.PROJECT_API_REPORT_DELETE)
    public void batchDelete(@Validated @RequestBody ApiReportBatchRequest request) {
        apiScenarioReportService.batchDelete(request, SessionUtils.getUserId());
    }

    @PostMapping("/batch-param")
    @Operation(summary = "接口测试-接口报告-获取场景报告批量参数")
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    @RequiresPermissions(PermissionConstants.PROJECT_API_REPORT_READ)
    public List<String> batchParam(@Validated @RequestBody ApiReportBatchRequest request) {
        return apiScenarioReportService.doSelectIds(request);
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "接口测试-接口报告-报告获取")
    @CheckOwner(resourceId = "#id", resourceType = "api_scenario_report")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_API_REPORT_READ, PermissionConstants.PROJECT_API_SCENARIO_EXECUTE}, logical = Logical.OR)
    public ApiScenarioReportDTO get(@PathVariable String id) {
        return apiScenarioReportService.get(id);
    }

    @GetMapping("/share/{shareId}/{reportId}")
    @Operation(summary = "接口测试-接口报告-分享报告获取")
    public ApiScenarioReportDTO get(@PathVariable String shareId, @PathVariable String reportId) {
        ShareInfo shareInfo = apiReportShareService.checkResource(shareId);
        apiReportShareService.validateExpired(shareInfo);
        return apiScenarioReportService.get(reportId);
    }

    @GetMapping("/get/detail/{reportId}/{stepId}")
    @Operation(summary = "接口测试-接口报告-报告详情获取")
    @CheckOwner(resourceId = "#reportId", resourceType = "api_scenario_report")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_API_REPORT_READ, PermissionConstants.PROJECT_API_SCENARIO_EXECUTE}, logical = Logical.OR)
    public List<ApiScenarioReportDetailDTO> getDetail(@PathVariable String reportId,
                                                      @PathVariable String stepId) {
        return apiScenarioReportService.getDetail(reportId, stepId);
    }

    @GetMapping("/share/detail/{shareId}/{reportId}/{stepId}")
    public List<ApiScenarioReportDetailDTO> selectReportContent(@PathVariable String shareId,
                                                                @PathVariable String reportId,
                                                                @PathVariable String stepId) {
        ShareInfo shareInfo = apiReportShareService.checkResource(shareId);
        apiReportShareService.validateExpired(shareInfo);
        return apiScenarioReportService.getDetail(reportId, stepId);
    }

    @PostMapping("/export/{reportId}")
    @Operation(summary = "接口测试-场景报告-导出日志")
    @RequiresPermissions(PermissionConstants.PROJECT_API_REPORT_EXPORT)
    public void exportLog(@PathVariable String reportId) {
        apiScenarioReportService.exportLog(reportId, SessionUtils.getUserId(), SessionUtils.getCurrentProjectId());
    }

    @PostMapping("/batch-export")
    @Operation(summary = "接口测试-场景报告-导出日志")
    @RequiresPermissions(PermissionConstants.PROJECT_API_REPORT_EXPORT)
    public void batchExportLog(@Validated @RequestBody ApiReportBatchRequest request) {
        apiScenarioReportService.batchExportLog(request, SessionUtils.getUserId(), SessionUtils.getCurrentProjectId());
    }


    @GetMapping("/task-step/{id}")
    @Operation(summary = "系统-任务中心-场景用例执行任务详情-查看(任务步骤)")
    @RequiresPermissions(value = {PermissionConstants.SYSTEM_CASE_TASK_CENTER_READ,
            PermissionConstants.ORGANIZATION_CASE_TASK_CENTER_READ, PermissionConstants.PROJECT_CASE_TASK_CENTER_READ,
            PermissionConstants.PROJECT_API_REPORT_READ, PermissionConstants.PROJECT_API_SCENARIO_EXECUTE}, logical = Logical.OR)
    public ExecTaskDetailDTO viewScenarioItemReport(@PathVariable String id) {
        return apiScenarioReportService.viewScenarioItemReport(id);
    }

    @GetMapping("/task-report/{reportId}/{stepId}")
    @Operation(summary = "系统-任务中心-场景用例执行任务详情-查看(步骤结果)")
    @RequiresPermissions(value = {PermissionConstants.SYSTEM_CASE_TASK_CENTER_READ,
            PermissionConstants.ORGANIZATION_CASE_TASK_CENTER_READ, PermissionConstants.PROJECT_CASE_TASK_CENTER_READ,
            PermissionConstants.PROJECT_API_REPORT_READ, PermissionConstants.PROJECT_API_SCENARIO_EXECUTE}, logical = Logical.OR)
    public List<ApiScenarioReportDetailDTO> getScenarioReportDetail(@PathVariable String reportId,
                                                                    @PathVariable String stepId) {
        return apiScenarioReportService.getDetail(reportId, stepId);
    }
}
