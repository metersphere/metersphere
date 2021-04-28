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
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.ScheduleRequest;
import io.metersphere.track.request.testcase.ApiCaseRelevanceRequest;
import io.metersphere.track.request.testplan.FileOperationRequest;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/api/automation")
@RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER}, logical = Logical.OR)
public class ApiAutomationController {

    @Resource
    ApiAutomationService apiAutomationService;

    @PostMapping("/list/{goPage}/{pageSize}")
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
    public Pager<List<ApiScenarioDTO>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiScenarioRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        return PageUtils.setPageInfo(page, apiAutomationService.list(request));
    }

    @PostMapping("/list/all")
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
    public List<ApiScenarioWithBLOBs> listAll(@RequestBody ApiScenarioBatchRequest request) {
        return apiAutomationService.listAll(request);
    }

    @PostMapping("/id/all")
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
    public List<String> idAll(@RequestBody ApiScenarioBatchRequest request) {
        return apiAutomationService.idAll(request);
    }

    @GetMapping("/list/{projectId}")
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
    public List<ApiScenarioDTO> list(@PathVariable String projectId) {
        ApiScenarioRequest request = new ApiScenarioRequest();
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        request.setProjectId(projectId);
        return apiAutomationService.list(request);
    }

    @PostMapping(value = "/create")
    public ApiScenario create(@RequestPart("request") SaveApiScenarioRequest request, @RequestPart(value = "files") List<MultipartFile> bodyFiles) {
        return apiAutomationService.create(request, bodyFiles);
    }

    @PostMapping(value = "/update")
    public void update(@RequestPart("request") SaveApiScenarioRequest request, @RequestPart(value = "files") List<MultipartFile> bodyFiles) {
        apiAutomationService.update(request, bodyFiles);
    }

    @GetMapping("/delete/{id}")
    public void delete(@PathVariable String id) {
        apiAutomationService.delete(id);
    }

    @PostMapping("/deleteBatch")
    public void deleteBatch(@RequestBody List<String> ids) {
        apiAutomationService.deleteBatch(ids);
    }

    @PostMapping("/deleteBatchByCondition")
    public void deleteBatchByCondition(@RequestBody ApiScenarioBatchRequest request) {
        apiAutomationService.deleteBatchByCondition(request);
    }

    @PostMapping("/removeToGc")
    public void removeToGc(@RequestBody List<String> ids) {
        apiAutomationService.removeToGc(ids);
    }

    @PostMapping("/removeToGcByBatch")
    public void removeToGcByBatch(@RequestBody ApiScenarioBatchRequest request) {
        apiAutomationService.removeToGcByBatch(request);
    }

    @PostMapping("/reduction")
    public void reduction(@RequestBody List<String> ids) {
        apiAutomationService.reduction(ids);
    }

    @GetMapping("/getApiScenario/{id}")
    public ApiScenarioWithBLOBs getScenarioDefinition(@PathVariable String id) {
        return apiAutomationService.getApiScenario(id);
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
    public List<ApiScenarioWithBLOBs> getApiScenarios(@RequestBody List<String> ids) {
        return apiAutomationService.getApiScenarios(ids);
    }

    @PostMapping(value = "/run/debug")
    public void runDebug(@RequestPart("request") RunDefinitionRequest request, @RequestPart(value = "files") List<MultipartFile> bodyFiles) {
        request.setExecuteType(ExecuteType.Debug.name());
        apiAutomationService.debugRun(request, bodyFiles);
    }

    @PostMapping(value = "/run")
    public String run(@RequestBody RunScenarioRequest request) {
        request.setExecuteType(ExecuteType.Completed.name());
        request.setTriggerMode(ApiRunMode.SCENARIO.name());
        request.setRunMode(ApiRunMode.SCENARIO.name());
        return apiAutomationService.run(request);
    }

    @PostMapping(value = "/run/jenkins")
    public String runByJenkins(@RequestBody RunScenarioRequest request) {
        request.setExecuteType(ExecuteType.Saved.name());
        request.setTriggerMode(ApiRunMode.API.name());
        request.setRunMode(ApiRunMode.SCENARIO.name());
        return apiAutomationService.run(request);
    }

    @PostMapping(value = "/run/batch")
    public String runBatch(@RequestBody RunScenarioRequest request) {
        request.setExecuteType(ExecuteType.Saved.name());
        request.setTriggerMode(ApiRunMode.SCENARIO.name());
        request.setRunMode(ApiRunMode.SCENARIO.name());
        return apiAutomationService.run(request);
    }

    @PostMapping("/batch/edit")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public void bathEdit(@RequestBody ApiScenarioBatchRequest request) {
        apiAutomationService.bathEdit(request);
    }

    @PostMapping("/batch/update/env")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
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
    public JmxInfoDTO genPerformanceTestJmx(@RequestBody RunScenarioRequest runRequest) throws Exception {
        runRequest.setExecuteType(ExecuteType.Completed.name());
        return apiAutomationService.genPerformanceTestJmx(runRequest);
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
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public ScenarioImport scenarioImport(@RequestPart(value = "file", required = false) MultipartFile file, @RequestPart("request") ApiTestImportRequest request) {
        return apiAutomationService.scenarioImport(file, request);
    }

    @PostMapping(value = "/export")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public ApiScenrioExportResult export(@RequestBody ApiScenarioBatchRequest request) {
        return apiAutomationService.export(request);
    }

    @PostMapping(value = "/export/jmx")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public List<ApiScenrioExportJmx> exportJmx(@RequestBody ApiScenarioBatchRequest request) {
        return apiAutomationService.exportJmx(request);
    }

}

