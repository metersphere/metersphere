package io.metersphere.controller.scenario;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.*;
import io.metersphere.api.dto.automation.*;
import io.metersphere.api.dto.definition.RunDefinitionRequest;
import io.metersphere.api.dto.export.ScenarioToPerformanceInfoDTO;
import io.metersphere.api.parse.scenario.ScenarioImport;
import io.metersphere.base.domain.ApiScenario;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.base.domain.Schedule;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.WebSocketUtil;
import io.metersphere.dto.BaseCase;
import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.notice.annotation.SendNotice;
import io.metersphere.request.ResetOrderRequest;
import io.metersphere.service.ext.ExtApiTaskService;
import io.metersphere.service.scenario.ApiScenarioService;
import io.metersphere.task.dto.TaskRequestDTO;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/automation")
public class ApiScenarioController {

    @Resource
    private ApiScenarioService apiAutomationService;
    @Resource
    private ExtApiTaskService apiTaskService;

    @PostMapping("/list/{goPage}/{pageSize}")
    @RequiresPermissions("PROJECT_API_SCENARIO:READ")
    public Pager<List<ApiScenarioDTO>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiScenarioRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        // 查询场景环境
        request.setSelectEnvironment(true);
        return PageUtils.setPageInfo(page, apiAutomationService.list(request));
    }

    @PostMapping("/scenario/schedule")
    @RequiresPermissions("PROJECT_API_SCENARIO:READ")
    public Map<String, ScheduleDTO> scenarioScheduleInfo(@RequestBody List<String> scenarioIds) {
        return apiAutomationService.selectScheduleInfo(scenarioIds);
    }

    @PostMapping("/list")
    @RequiresPermissions("PROJECT_API_SCENARIO:READ")
    public List<ApiScenarioDTO> listAll(@RequestBody ApiScenarioRequest request) {
        return apiAutomationService.list(request);
    }

    @PostMapping("/select/by/id")
    public List<ApiScenario> selectByIds(@RequestBody ApiScenarioRequest request) {
        if (CollectionUtils.isNotEmpty(request.getIds())) {
            return apiAutomationService.selectByIds(request.getIds());
        } else {
            return new ArrayList<>(0);
        }
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

    @PostMapping("/list-blobs")
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
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.CREATE, title = "#request.name", content = "#msClass.getLogDetails(#request.id)", msClass = ApiScenarioService.class)
    @RequiresPermissions(value = {PermissionConstants.PROJECT_API_SCENARIO_READ_CREATE, PermissionConstants.PROJECT_API_SCENARIO_READ_COPY}, logical = Logical.OR)
    @SendNotice(taskType = NoticeConstants.TaskType.API_AUTOMATION_TASK, event = NoticeConstants.Event.CREATE, subject = "接口自动化通知")
    public ApiScenario create(@RequestPart("request") SaveApiScenarioRequest request, @RequestPart(value = "bodyFiles", required = false) List<MultipartFile> bodyFiles, @RequestPart(value = "scenarioFiles", required = false) List<MultipartFile> scenarioFiles) {
        return apiAutomationService.create(request, bodyFiles, scenarioFiles);
    }

    @PostMapping(value = "/update")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request.id)", title = "#request.name", content = "#msClass.getLogDetails(#request.id)", msClass = ApiScenarioService.class)
    @RequiresPermissions(value = {PermissionConstants.PROJECT_API_SCENARIO_READ_EDIT, PermissionConstants.PROJECT_API_SCENARIO_READ_COPY}, logical = Logical.OR)
    @SendNotice(taskType = NoticeConstants.TaskType.API_AUTOMATION_TASK, event = NoticeConstants.Event.UPDATE, subject = "接口自动化通知")
    public ApiScenario update(@RequestPart("request") SaveApiScenarioRequest request, @RequestPart(value = "bodyFiles", required = false) List<MultipartFile> bodyFiles, @RequestPart(value = "scenarioFiles", required = false) List<MultipartFile> scenarioFiles) {
        return apiAutomationService.update(request, bodyFiles, scenarioFiles);
    }


    @PostMapping("/sort")
    public void orderCase(@RequestBody ResetOrderRequest request) {
        apiAutomationService.updateOrder(request);
    }

    @GetMapping("/delete/{id}")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = ApiScenarioService.class)
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_READ_DELETE)
    public void delete(@PathVariable String id) {
        apiAutomationService.delete(id);
    }

    @PostMapping("/del-ids")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.BATCH_DEL, beforeEvent = "#msClass.getLogDetails(#ids)", msClass = ApiScenarioService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.API_AUTOMATION_TASK, event = NoticeConstants.Event.DELETE, target = "#targetClass.getScenarioCaseByIds(#ids)", targetClass = ApiScenarioService.class, subject = "接口自动化通知")
    public void deleteBatch(@RequestBody List<String> ids) {
        apiAutomationService.deleteBatch(ids);
    }

    @PostMapping("/del-batch")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.BATCH_DEL, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = ApiScenarioService.class)
    public void deleteBatchByCondition(@RequestBody ApiScenarioBatchRequest request) {
        apiAutomationService.deleteBatchByCondition(request);
    }

    @PostMapping("/move-gc-ids")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.GC, beforeEvent = "#msClass.getLogDetails(#ids)", msClass = ApiScenarioService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.API_AUTOMATION_TASK, target = "#targetClass.getScenarioCaseByIds(#ids)", targetClass = ApiScenarioService.class, event = NoticeConstants.Event.DELETE, subject = "接口自动化通知")
    public void removeToGc(@RequestBody List<String> ids) {
        apiAutomationService.removeToGc(ids);
    }

    @PostMapping("/move-gc-batch")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.BATCH_GC, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = ApiScenarioService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.API_AUTOMATION_TASK, target = "#targetClass.getScenarioCaseByIds(#request.ids)", targetClass = ApiScenarioService.class, event = NoticeConstants.Event.DELETE, subject = "接口自动化通知")
    public void removeToGcByBatch(@RequestBody ApiScenarioBatchRequest request) {
        apiAutomationService.removeToGcByBatch(request);
    }

    @PostMapping("/get-del-details")
    public DeleteCheckResult checkBeforeDelete(@RequestBody ApiScenarioBatchRequest request) {
        DeleteCheckResult checkResult = apiAutomationService.checkBeforeDelete(request);
        return checkResult;
    }

    @PostMapping("/reduction")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.RESTORE, beforeEvent = "#msClass.getLogDetails(#ids)", msClass = ApiScenarioService.class)
    public void reduction(@RequestBody List<String> ids) {
        apiAutomationService.reduction(ids);
    }

    @GetMapping("/scenario-details/{id}")
    public ApiScenarioDTO getScenarioDefinition(@PathVariable String id) {
        return apiAutomationService.getNewApiScenario(id);
    }

    @PostMapping("/scenario-env")
    public ScenarioEnv getScenarioDefinition(@RequestBody ApiScenarioEnvRequest request) {
        return apiAutomationService.getApiScenarioEnv(request.getDefinition());
    }

    @GetMapping("/env-project-ids/{id}")
    public ScenarioEnv getApiScenarioProjectId(@PathVariable String id) {
        return apiAutomationService.getApiScenarioProjectId(id);
    }

    @PostMapping("/list-project-ids")
    public List<ScenarioIdProjectInfo> getApiScenarioProjectIdByConditions(@RequestBody ApiScenarioBatchRequest request) {
        return apiAutomationService.getApiScenarioProjectIdByConditions(request);
    }

    @PostMapping("/get-scenario-list")
    public List<ApiScenarioDTO> getApiScenarios(@RequestBody List<String> ids) {
        return apiAutomationService.getNewApiScenarios(ids);
    }

    @PostMapping(value = "/run/debug")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.DEBUG, title = "#request.scenarioName", sourceId = "#request.scenarioId", project = "#request.projectId")
    public String runDebug(@RequestPart("request") RunDefinitionRequest request, @RequestPart(value = "bodyFiles", required = false) List<MultipartFile> bodyFiles, @RequestPart(value = "scenarioFiles", required = false) List<MultipartFile> scenarioFiles) {
        try {
            if (StringUtils.isEmpty(request.getExecuteType())) {
                request.setExecuteType(ExecuteType.Debug.name());
            }
            request.setTriggerMode(TriggerMode.MANUAL.name());
            apiAutomationService.debugRun(request, bodyFiles, scenarioFiles);
        } catch (Exception e) {
            return e.getMessage();
        }
        return "SUCCESS";
    }

    @PostMapping(value = "/run")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.EXECUTE, content = "#msClass.getLogDetails(#request.ids)", msClass = ApiScenarioService.class)
    public List<MsExecResponseDTO> run(@RequestBody RunScenarioRequest request) {
        if (!StringUtils.equals(request.getExecuteType(), ExecuteType.Saved.name())) {
            request.setExecuteType(ExecuteType.Completed.name());
        }
        request.setTriggerMode(TriggerMode.MANUAL.name());
        request.setRunMode(ApiRunMode.SCENARIO.name());
        return apiAutomationService.run(request);
    }

    @PostMapping(value = "/plan/run")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.EXECUTE, content = "#msClass.getLogDetails(#request.ids)", msClass = ApiScenarioService.class)
    public List<MsExecResponseDTO> planRun(@RequestBody RunScenarioRequest request) {
        return apiAutomationService.run(request);
    }

    @PostMapping(value = "/run/jenkins")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.EXECUTE, content = "#msClass.getLogDetails(#request.id)", msClass = ApiScenarioService.class)
    public List<MsExecResponseDTO> runByJenkins(@RequestBody RunScenarioRequest request) {
        request.setExecuteType(ExecuteType.Saved.name());
        request.setTriggerMode(TriggerMode.API.name());
        request.setRunMode(ApiRunMode.SCENARIO.name()); // 回退
        return apiAutomationService.run(request);
    }

    @PostMapping(value = "/run/batch")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.EXECUTE, content = "#msClass.getLogDetails(#request.ids)", msClass = ApiScenarioService.class)
    public List<MsExecResponseDTO> runBatch(@RequestBody RunScenarioRequest request) {
        request.setExecuteType(ExecuteType.Saved.name());
        request.setTriggerMode(TriggerMode.BATCH.name());
        request.setRunMode(ApiRunMode.SCENARIO.name());
        return apiAutomationService.run(request);
    }

    @PostMapping("/batch/edit")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_READ_EDIT)
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.BATCH_UPDATE, beforeEvent = "#msClass.getLogDetails(#request.ids)", content = "#msClass.getLogDetails(#request.ids)", msClass = ApiScenarioService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.API_AUTOMATION_TASK, event = NoticeConstants.Event.UPDATE, target = "#targetClass.getScenarioCaseByIds(#request.ids)", targetClass = ApiScenarioService.class, subject = "接口自动化通知")
    public void bathEdit(@RequestBody ApiScenarioBatchRequest request) {
        apiAutomationService.bathEdit(request);
    }

    @PostMapping("/batch/copy")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_API_SCENARIO_READ_CREATE, PermissionConstants.PROJECT_API_SCENARIO_READ_COPY}, logical = Logical.OR)
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.BATCH_ADD, beforeEvent = "#msClass.getLogDetails(#request.ids)", content = "#msClass.getLogDetails(#request.ids)", msClass = ApiScenarioService.class)
    public void batchCopy(@RequestBody ApiScenarioBatchRequest request) {
        apiAutomationService.batchCopy(request);
    }

    @PostMapping("/batch/update/env")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_READ_EDIT)
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.BATCH_UPDATE, beforeEvent = "#msClass.getLogDetails(#request.ids)", content = "#msClass.getLogDetails(#request.ids)", msClass = ApiScenarioService.class)
    public void batchUpdateEnv(@RequestBody ApiScenarioBatchRequest request) {
        apiAutomationService.batchUpdateEnv(request);
    }

    @PostMapping("/scenario/plan")
    public String addScenarioToPlan(@RequestBody SaveApiPlanRequest request) {
        return apiAutomationService.addScenarioToPlan(request);
    }

    @PostMapping(value = "/schedule/update")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION_SCHEDULE, type = OperLogConstants.UPDATE, title = "#request.name", beforeEvent = "#msClass.getLogDetails(#request.id)", content = "#msClass.getLogDetails(#request.id)", msClass = ApiScenarioService.class)
    public void updateSchedule(@RequestBody Schedule request) {
        apiAutomationService.updateSchedule(request);
    }

    @PostMapping(value = "/schedule/create")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION_SCHEDULE, type = OperLogConstants.CREATE, title = "#request.name", content = "#msClass.getLogDetails(#request)", msClass = ApiScenarioService.class)
    public void createSchedule(@RequestBody ScheduleRequest request) {
        apiAutomationService.createSchedule(request);
    }

    @PostMapping(value = "/gen-jmx")
    public ScenarioToPerformanceInfoDTO genPerformanceTestJmx(@RequestBody GenScenarioRequest runRequest) throws Exception {
        runRequest.setExecuteType(ExecuteType.Completed.name());
        return apiAutomationService.genPerformanceTestJmx(runRequest);
    }

    @PostMapping("/gen-jmx-batch")
    public ScenarioToPerformanceInfoDTO batchGenPerformanceTestJmx(@RequestBody ApiScenarioBatchRequest request) {
        return apiAutomationService.batchGenPerformanceTestJmx(request);
    }

    @PostMapping("/file/download")
    public ResponseEntity<byte[]> download(@RequestBody FileOperationRequest fileOperationRequest) {
        byte[] bytes = apiAutomationService.loadFileAsBytes(fileOperationRequest);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/octet-stream")).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileOperationRequest.getName() + "\"").body(bytes);
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
            List<TaskRequestDTO> reportIds = new ArrayList<>();
            TaskRequestDTO taskRequest = new TaskRequestDTO();
            taskRequest.setReportId(reportId);
            taskRequest.setType(ElementConstants.SCENARIO_UPPER);
            reportIds.add(taskRequest);

            WebSocketUtil.onClose(reportId);
            apiTaskService.apiStop(reportIds);
        }
    }

    @PostMapping(value = "/stop/batch")
    public void stopBatch(@RequestBody List<TaskRequestDTO> reportIds) {
        apiTaskService.apiStop(reportIds);
    }

    @PostMapping("/set-domain")
    public String setDomain(@RequestBody ApiScenarioEnvRequest request) {
        return apiAutomationService.setDomain(request);
    }

    @PostMapping(value = "/export/zip")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_READ_EXPORT_SCENARIO)
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.EXPORT, sourceId = "#request.id", title = "#request.name", project = "#request.projectId")
    public ResponseEntity<byte[]> downloadBodyFiles(@RequestBody ApiScenarioBatchRequest request) {
        try {
            byte[] bytes = apiAutomationService.exportZip(request);
            return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/octet-stream")).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "场景JMX文件集.zip").body(bytes);
        } catch (Exception e) {
            return ResponseEntity.status(509).body(e.getMessage().getBytes());
        }
    }

    @PostMapping(value = "/export/jmx")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_READ_EXPORT_SCENARIO)
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.EXPORT, sourceId = "#request.id", title = "#request.name", project = "#request.projectId")
    public ScenarioToPerformanceInfoDTO exportJmx(@RequestBody ApiScenarioBatchRequest request) {
        return apiAutomationService.exportJmx(request);
    }

    @PostMapping(value = "/env/valid")
    public boolean checkScenarioEnv(@RequestBody ApiScenarioWithBLOBs request) {
        try {
            return apiAutomationService.verifyScenarioEnv(request);
        } catch (Exception e) {
            MSException.throwException(Translator.get("scenario_step_parsing_error_check"));
        }
        return false;
    }

    @GetMapping(value = "/env-valid/{scenarioId}")
    public boolean checkScenarioEnvByScenarioId(@PathVariable String scenarioId) {
        try {
            return apiAutomationService.verifyScenarioEnv(scenarioId);
        } catch (Exception e) {
            MSException.throwException(Translator.get("scenario_step_parsing_error_check"));
        }
        return false;
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

    @PostMapping(value = "/env/map")
    public Map<String, List<String>> getProjectEnvMap(@RequestBody RunScenarioRequest request) {
        return apiAutomationService.getProjectEnvMap(request);
    }

    /**
     * 统计场景用例
     * By.jianguo：
     * 项目报告服务需要统计场景用例
     */
    @PostMapping("/count")
    public List<ApiCountChartResult> countScenarioCaseByRequest(@RequestBody ApiCountRequest request) {
        return apiAutomationService.countByRequest(request);
    }

    @GetMapping("/get-base-case/{projectId}")
    @RequiresPermissions("PROJECT_API_SCENARIO:READ")
    public List<BaseCase> getBaseCaseByProjectId(@PathVariable String projectId) {
        return apiAutomationService.getBaseCaseByProjectId(projectId);
    }
}

