package io.metersphere.api.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.*;
import io.metersphere.api.dto.automation.*;
import io.metersphere.api.dto.automation.parse.ScenarioImport;
import io.metersphere.api.dto.definition.RunDefinitionRequest;
import io.metersphere.api.service.ApiAutomationService;
import io.metersphere.base.domain.ApiScenario;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.base.domain.Schedule;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.controller.request.ResetOrderRequest;
import io.metersphere.controller.request.ScheduleRequest;
import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.notice.annotation.SendNotice;
import io.metersphere.task.service.TaskService;
import io.metersphere.track.request.testcase.ApiCaseRelevanceRequest;
import io.metersphere.track.request.testplan.FileOperationRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/api/automation")
public class ApiAutomationController {

    @Resource
    private ApiAutomationService apiAutomationService;
    @Resource
    private TaskService taskService;

    @PostMapping("/list/{goPage}/{pageSize}")
    @RequiresPermissions("PROJECT_API_SCENARIO:READ")
    public Pager<List<ApiScenarioDTO>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiScenarioRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        // 查询场景环境
        request.setSelectEnvironment(true);
        return PageUtils.setPageInfo(page, apiAutomationService.list(request));
    }

    @PostMapping("/list")
    @RequiresPermissions("PROJECT_API_SCENARIO:READ")
    public List<ApiScenarioDTO> listAll(@RequestBody ApiScenarioRequest request) {

        return apiAutomationService.list(request);
    }

    @GetMapping("/get/{id}")
    @RequiresPermissions("PROJECT_API_SCENARIO:READ")
    public ApiScenarioDTO getById(@PathVariable String id) {
        return apiAutomationService.getDto(id);
    }

    @PostMapping("/list/all")
    @RequiresPermissions("PROJECT_API_SCENARIO:READ")
    public List<ApiScenarioDTO> listAll(@RequestBody ApiScenarioBatchRequest request) {
        return apiAutomationService.listAll(request);
    }

    @PostMapping("/list/all/trash")
    public int listAllTrash(@RequestBody ApiScenarioBatchRequest request) {
        return apiAutomationService.listAllTrash(request);
    }

    @PostMapping("/listWithIds/all")
    @RequiresPermissions("PROJECT_API_SCENARIO:READ")
    public List<ApiScenarioWithBLOBs> listWithIds(@RequestBody ApiScenarioBatchRequest request) {
        return apiAutomationService.listWithIds(request);
    }


    @PostMapping("/id/all")
    @RequiresPermissions("PROJECT_API_SCENARIO:READ")
    public List<String> idAll(@RequestBody ApiScenarioBatchRequest request) {
        return apiAutomationService.idAll(request);
    }

    @GetMapping("/list/{projectId}")
    @RequiresPermissions("PROJECT_API_SCENARIO:READ")
    public List<ApiScenarioDTO> list(@PathVariable String projectId) {
        ApiScenarioRequest request = new ApiScenarioRequest();
        request.setProjectId(projectId);
        return apiAutomationService.list(request);
    }

    @PostMapping(value = "/create")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.CREATE, title = "#request.name", content = "#msClass.getLogDetails(#request.id)", msClass = ApiAutomationService.class)
    @RequiresPermissions(value = {PermissionConstants.PROJECT_API_SCENARIO_READ_CREATE, PermissionConstants.PROJECT_API_SCENARIO_READ_COPY}, logical = Logical.OR)
    @SendNotice(taskType = NoticeConstants.TaskType.API_AUTOMATION_TASK, event = NoticeConstants.Event.CREATE, mailTemplate = "api/AutomationCreate", subject = "接口自动化通知")
    public ApiScenario create(@RequestPart("request") SaveApiScenarioRequest request, @RequestPart(value = "bodyFiles", required = false) List<MultipartFile> bodyFiles,
                              @RequestPart(value = "scenarioFiles", required = false) List<MultipartFile> scenarioFiles) {
        return apiAutomationService.create(request, bodyFiles, scenarioFiles);
    }

    @PostMapping(value = "/update")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request.id)", title = "#request.name", content = "#msClass.getLogDetails(#request.id)", msClass = ApiAutomationService.class)
    @RequiresPermissions(value = {PermissionConstants.PROJECT_API_SCENARIO_READ_EDIT, PermissionConstants.PROJECT_API_SCENARIO_READ_COPY}, logical = Logical.OR)
    @SendNotice(taskType = NoticeConstants.TaskType.API_AUTOMATION_TASK, event = NoticeConstants.Event.UPDATE, mailTemplate = "api/AutomationUpdate", subject = "接口自动化通知")
    public ApiScenario update(@RequestPart("request") SaveApiScenarioRequest request, @RequestPart(value = "bodyFiles", required = false) List<MultipartFile> bodyFiles,
                              @RequestPart(value = "scenarioFiles", required = false) List<MultipartFile> scenarioFiles) {
        return apiAutomationService.update(request, bodyFiles, scenarioFiles);
    }


    @PostMapping("/edit/order")
    public void orderCase(@RequestBody ResetOrderRequest request) {
        apiAutomationService.updateOrder(request);
    }

    @GetMapping("/delete/{id}")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = ApiAutomationService.class)
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_READ_DELETE)
    public void delete(@PathVariable String id) {
        apiAutomationService.delete(id);
    }

    @PostMapping("/deleteBatch")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.BATCH_DEL, beforeEvent = "#msClass.getLogDetails(#ids)", msClass = ApiAutomationService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.API_AUTOMATION_TASK, event = NoticeConstants.Event.DELETE, target = "#targetClass.getScenarioCaseByIds(#ids)", targetClass = ApiAutomationService.class,
            mailTemplate = "api/AutomationUpdate", subject = "接口自动化通知")
    public void deleteBatch(@RequestBody List<String> ids) {
        apiAutomationService.deleteBatch(ids);
    }

    @PostMapping("/deleteBatchByCondition")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.BATCH_DEL, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = ApiAutomationService.class)
    public void deleteBatchByCondition(@RequestBody ApiScenarioBatchRequest request) {
        apiAutomationService.deleteBatchByCondition(request);
    }

    @PostMapping("/removeToGc")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.GC, beforeEvent = "#msClass.getLogDetails(#ids)", msClass = ApiAutomationService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.API_AUTOMATION_TASK, target = "#targetClass.getApiScenarios(#ids)", targetClass = ApiAutomationService.class,
            event = NoticeConstants.Event.DELETE, mailTemplate = "api/AutomationDelete", subject = "接口自动化通知")
    public void removeToGc(@RequestBody List<String> ids) {
        apiAutomationService.removeToGc(ids);
    }

    @PostMapping("/removeToGcByBatch")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.BATCH_GC, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = ApiAutomationService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.API_AUTOMATION_TASK, target = "#targetClass.getApiScenarios(#request.ids)", targetClass = ApiAutomationService.class,
            event = NoticeConstants.Event.DELETE, mailTemplate = "api/AutomationDelete", subject = "接口自动化通知")
    public void removeToGcByBatch(@RequestBody ApiScenarioBatchRequest request) {
        apiAutomationService.removeToGcByBatch(request);
    }

    @PostMapping("/checkBeforeDelete")
    public DeleteCheckResult checkBeforeDelete(@RequestBody ApiScenarioBatchRequest request) {
        DeleteCheckResult checkResult = apiAutomationService.checkBeforeDelete(request);
        return checkResult;
    }

    @PostMapping("/reduction")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.RESTORE, beforeEvent = "#msClass.getLogDetails(#ids)", msClass = ApiAutomationService.class)
    public void reduction(@RequestBody List<String> ids) {
        apiAutomationService.reduction(ids);
    }

    @GetMapping("/getApiScenario/{id}")
    public ApiScenarioDTO getScenarioDefinition(@PathVariable String id) {
        return apiAutomationService.getNewApiScenario(id);
    }

    @PostMapping("/getApiScenarioEnv")
    public ScenarioEnv getScenarioDefinition(@RequestBody ApiScenarioEnvRequest request) {
        return apiAutomationService.getApiScenarioEnv(request.getDefinition());
    }

    @GetMapping("/getApiScenarioProjectId/{id}")
    public ScenarioEnv getApiScenarioProjectId(@PathVariable String id) {
        return apiAutomationService.getApiScenarioProjectId(id);
    }

    @PostMapping("/getApiScenarioProjectIdByConditions")
    public List<ScenarioIdProjectInfo> getApiScenarioProjectIdByConditions(@RequestBody ApiScenarioBatchRequest request) {
        return apiAutomationService.getApiScenarioProjectIdByConditions(request);
    }

    @PostMapping("/getApiScenarios")
    public List<ApiScenarioDTO> getApiScenarios(@RequestBody List<String> ids) {
        return apiAutomationService.getNewApiScenarios(ids);
    }

    @PostMapping(value = "/run/debug")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.DEBUG, title = "#request.scenarioName", sourceId = "#request.scenarioId", project = "#request.projectId")
    public void runDebug(@RequestPart("request") RunDefinitionRequest request,
                         @RequestPart(value = "bodyFiles", required = false) List<MultipartFile> bodyFiles, @RequestPart(value = "scenarioFiles", required = false) List<MultipartFile> scenarioFiles) {
        if (StringUtils.isEmpty(request.getExecuteType())) {
            request.setExecuteType(ExecuteType.Debug.name());
        }
        apiAutomationService.debugRun(request, bodyFiles, scenarioFiles);
    }

    @PostMapping(value = "/run")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.EXECUTE, content = "#msClass.getLogDetails(#request.ids)", msClass = ApiAutomationService.class)
    public List<MsExecResponseDTO> run(@RequestBody RunScenarioRequest request) {
        if (!StringUtils.equals(request.getExecuteType(), ExecuteType.Saved.name())) {
            request.setExecuteType(ExecuteType.Completed.name());
        }
        request.setTriggerMode(TriggerMode.MANUAL.name());
        request.setRunMode(ApiRunMode.SCENARIO.name());
        return apiAutomationService.run(request);
    }

    @PostMapping(value = "/run/jenkins")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.EXECUTE, content = "#msClass.getLogDetails(#request.id)", msClass = ApiAutomationService.class)
    public List<MsExecResponseDTO> runByJenkins(@RequestBody RunScenarioRequest request) {
        request.setExecuteType(ExecuteType.Saved.name());
        request.setTriggerMode(TriggerMode.API.name());
        request.setRunMode(ApiRunMode.SCENARIO.name()); // 回退
        return apiAutomationService.run(request);
    }

    @PostMapping(value = "/run/batch")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.EXECUTE, content = "#msClass.getLogDetails(#request.ids)", msClass = ApiAutomationService.class)
    public List<MsExecResponseDTO> runBatch(@RequestBody RunScenarioRequest request) {
        request.setExecuteType(ExecuteType.Saved.name());
        request.setTriggerMode(TriggerMode.BATCH.name());
        request.setRunMode(ApiRunMode.SCENARIO.name());
        return apiAutomationService.run(request);
    }

    @PostMapping("/batch/edit")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_READ_EDIT)
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.BATCH_UPDATE, beforeEvent = "#msClass.getLogDetails(#request.ids)", content = "#msClass.getLogDetails(#request.ids)", msClass = ApiAutomationService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.API_AUTOMATION_TASK, event = NoticeConstants.Event.UPDATE, target = "#targetClass.getScenarioCaseByIds(#request.ids)", targetClass = ApiAutomationService.class,
            mailTemplate = "api/AutomationUpdate", subject = "接口自动化通知")
    public void bathEdit(@RequestBody ApiScenarioBatchRequest request) {
        apiAutomationService.bathEdit(request);
    }

    @PostMapping("/batch/copy")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_API_SCENARIO_READ_CREATE, PermissionConstants.PROJECT_API_SCENARIO_READ_COPY}, logical = Logical.OR)
    @MsAuditLog(module = "api_automation", type = OperLogConstants.BATCH_ADD, beforeEvent = "#msClass.getLogDetails(#request.ids)", content = "#msClass.getLogDetails(#request.ids)", msClass = ApiAutomationService.class)
    public void batchCopy(@RequestBody ApiScenarioBatchRequest request) {
        apiAutomationService.batchCopy(request);
    }

    @PostMapping("/batch/update/env")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_READ_EDIT)
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.BATCH_UPDATE, beforeEvent = "#msClass.getLogDetails(#request.ids)", content = "#msClass.getLogDetails(#request.ids)", msClass = ApiAutomationService.class)
    public void batchUpdateEnv(@RequestBody ApiScenarioBatchRequest request) {
        apiAutomationService.batchUpdateEnv(request);
    }

    @PostMapping("/getReference")
    public ReferenceDTO getReference(@RequestBody ApiScenarioRequest request) {
        return apiAutomationService.getReference(request);
    }

    @PostMapping("/scenario/plan")
    public String addScenarioToPlan(@RequestBody SaveApiPlanRequest request) {
        return apiAutomationService.addScenarioToPlan(request);
    }

    @PostMapping("/relevance")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.ASSOCIATE_CASE, content = "#msClass.getLogDetails(#request)", msClass = ApiAutomationService.class)
    public void testPlanRelevance(@RequestBody ApiCaseRelevanceRequest request) {
        apiAutomationService.relevance(request);
    }

    @PostMapping("/relevance/review")
    public void testCaseReviewRelevance(@RequestBody ApiCaseRelevanceRequest request) {
        apiAutomationService.relevanceReview(request);
    }

    @PostMapping(value = "/schedule/update")
    public void updateSchedule(@RequestBody Schedule request) {
        apiAutomationService.updateSchedule(request);
    }

    @PostMapping(value = "/schedule/create")
    public void createSchedule(@RequestBody ScheduleRequest request) {
        apiAutomationService.createSchedule(request);
    }

    @PostMapping(value = "/genPerformanceTestJmx")
    public JmxInfoDTO genPerformanceTestJmx(@RequestBody GenScenarioRequest runRequest) throws Exception {
        runRequest.setExecuteType(ExecuteType.Completed.name());
        return apiAutomationService.genPerformanceTestJmx(runRequest);
    }

    @PostMapping("/batchGenPerformanceTestJmx")
    public List<JmxInfoDTO> batchGenPerformanceTestJmx(@RequestBody ApiScenarioBatchRequest request) {
        return apiAutomationService.batchGenPerformanceTestJmx(request);
    }

    @PostMapping("/file/download")
    public ResponseEntity<byte[]> download(@RequestBody FileOperationRequest fileOperationRequest) {
        byte[] bytes = apiAutomationService.loadFileAsBytes(fileOperationRequest);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileOperationRequest.getName() + "\"")
                .body(bytes);
    }

    @PostMapping(value = "/import", consumes = {"multipart/form-data"})
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_READ_IMPORT_SCENARIO)
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.IMPORT, sourceId = "#request.id", title = "#request.name", project = "#request.projectId")
    public ScenarioImport scenarioImport(@RequestPart(value = "file", required = false) MultipartFile file, @RequestPart("request") ApiTestImportRequest request) {
        return apiAutomationService.scenarioImport(file, request);
    }

    @PostMapping(value = "/export")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_READ_EXPORT_SCENARIO)
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.EXPORT, sourceId = "#request.id", title = "#request.name", project = "#request.projectId")
    public ApiScenrioExportResult export(@RequestBody ApiScenarioBatchRequest request) {
        return apiAutomationService.export(request);
    }

    @GetMapping(value = "/stop/{reportId}")
    public void stop(@PathVariable String reportId) {
        if (StringUtils.isNotEmpty(reportId)) {
            List<TaskRequest> reportIds = new ArrayList<>();
            TaskRequest taskRequest = new TaskRequest();
            taskRequest.setReportId(reportId);
            taskRequest.setType("SCENARIO");
            reportIds.add(taskRequest);
            taskService.stop(reportIds);
        }
    }

    @PostMapping(value = "/stop/batch")
    public String stopBatch(@RequestBody List<TaskRequest> reportIds) {
        return taskService.stop(reportIds);
    }

    @PostMapping("/setDomain")
    public String setDomain(@RequestBody ApiScenarioEnvRequest request) {
        return apiAutomationService.setDomain(request);
    }

    @PostMapping(value = "/export/zip")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_READ_EXPORT_SCENARIO)
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.EXPORT, sourceId = "#request.id", title = "#request.name", project = "#request.projectId")
    public ResponseEntity<byte[]> downloadBodyFiles(@RequestBody ApiScenarioBatchRequest request) {
        try {
            byte[] bytes = apiAutomationService.exportZip(request);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "场景JMX文件集.zip")
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.status(509).body(e.getMessage().getBytes());
        }
    }

    @PostMapping(value = "/export/jmx")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_READ_EXPORT_SCENARIO)
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.EXPORT, sourceId = "#request.id", title = "#request.name", project = "#request.projectId")
    public List<ApiScenarioExportJmxDTO> exportJmx(@RequestBody ApiScenarioBatchRequest request) {
        return apiAutomationService.exportJmx(request);
    }

    @PostMapping(value = "/checkScenarioEnv")
    public boolean checkScenarioEnv(@RequestBody ApiScenarioWithBLOBs request) {
        return apiAutomationService.checkScenarioEnv(request);
    }

    @GetMapping(value = "/checkScenarioEnv/{scenarioId}")
    public boolean checkScenarioEnvByScenarioId(@PathVariable String scenarioId) {
        return apiAutomationService.checkScenarioEnv(scenarioId);
    }

    @GetMapping("/follow/{scenarioId}")
    public List<String> getFollows(@PathVariable String scenarioId) {
        return apiAutomationService.getFollows(scenarioId);
    }

    @PostMapping("/update/follows/{scenarioId}")
    public void saveFollows(@PathVariable String scenarioId, @RequestBody List<String> follows) {
        apiAutomationService.saveFollows(scenarioId, follows);
    }

    @GetMapping("versions/{scenarioId}")
    public List<ApiScenarioDTO> getApiScenarioVersions(@PathVariable String scenarioId) {
        return apiAutomationService.getApiScenarioVersions(scenarioId);
    }

    @GetMapping("get/{version}/{refId}")
    public ApiScenarioDTO getApiScenario(@PathVariable String version, @PathVariable String refId) {
        return apiAutomationService.getApiScenarioByVersion(refId, version);
    }

    @GetMapping("delete/{version}/{refId}")
    public void deleteApiScenario(@PathVariable String version, @PathVariable String refId) {
        apiAutomationService.deleteApiScenarioByVersion(refId, version);
    }

    @PostMapping(value = "/env")
    public List<String> getEnvProjects(@RequestBody RunScenarioRequest request) {
        return apiAutomationService.getProjects(request);
    }
}

