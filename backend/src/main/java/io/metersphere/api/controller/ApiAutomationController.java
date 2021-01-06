package io.metersphere.api.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.automation.*;
import io.metersphere.api.dto.definition.RunDefinitionRequest;
import io.metersphere.api.service.ApiAutomationService;
import io.metersphere.base.domain.ApiScenario;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.base.domain.Schedule;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.track.request.testcase.ApiCaseRelevanceRequest;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
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

    @PostMapping("/removeToGc")
    public void removeToGc(@RequestBody List<String> ids) {
        apiAutomationService.removeToGc(ids);
    }

    @PostMapping("/reduction")
    public void reduction(@RequestBody List<SaveApiScenarioRequest> requests) {
        apiAutomationService.reduction(requests);
    }

    @GetMapping("/getApiScenario/{id}")
    public ApiScenario getScenarioDefinition(@PathVariable String id) {
        return apiAutomationService.getApiScenario(id);
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
        return apiAutomationService.run(request);
    }

    @PostMapping(value = "/run/batch")
    public String runBatch(@RequestBody RunScenarioRequest request) {
        request.setExecuteType(ExecuteType.Saved.name());
        return apiAutomationService.run(request);
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

    @PostMapping(value = "/schedule/update")
    public void updateSchedule(@RequestBody Schedule request) {
        apiAutomationService.updateSchedule(request);
    }

    @PostMapping(value = "/schedule/create")
    public void createSchedule(@RequestBody Schedule request) {
        apiAutomationService.createSchedule(request);
    }

}

