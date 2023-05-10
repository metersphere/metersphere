package io.metersphere.controller.plan;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.ApiCaseRelevanceRequest;
import io.metersphere.api.dto.RelevanceScenarioRequest;
import io.metersphere.api.dto.automation.*;
import io.metersphere.api.dto.plan.*;
import io.metersphere.base.domain.TestPlanReport;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.dto.PlanReportCaseDTO;
import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.log.annotation.MsRequestLog;
import io.metersphere.request.ResetOrderRequest;
import io.metersphere.service.plan.TestPlanScenarioCaseService;
import io.metersphere.service.scenario.ApiScenarioService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequestMapping("/test/plan/scenario/case")
@RestController
public class TestPlanScenarioCaseController {

    @Resource
    TestPlanScenarioCaseService testPlanScenarioCaseService;

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<ApiScenarioDTO>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody TestPlanScenarioRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testPlanScenarioCaseService.list(request));
    }

    @GetMapping("/list/failure/{planId}")
    public List<TestPlanScenarioDTO> getFailureList(@PathVariable String planId) {
        return testPlanScenarioCaseService.getFailureCases(planId);
    }

    @GetMapping("/list/error-report/{planId}")
    public List<TestPlanScenarioDTO> getErrorReportList(@PathVariable String planId) {
        return testPlanScenarioCaseService.getErrorReportCases(planId);
    }

    @GetMapping("/list/pending/{planId}")
    public List<TestPlanScenarioDTO> getUnExecuteCases(@PathVariable String planId) {
        return testPlanScenarioCaseService.getUnExecuteCases(planId);
    }

    @GetMapping("/list/all/{planId}")
    public List<TestPlanScenarioDTO> getAllList(@PathVariable String planId) {
        return testPlanScenarioCaseService.getAllCases(planId);
    }

    @GetMapping("/list/{planId}")
    public List<ApiScenarioDTO> getByPlanId(@PathVariable String planId) {
        TestPlanScenarioRequest request = new TestPlanScenarioRequest();
        request.setPlanId(planId);
        return testPlanScenarioCaseService.list(request);
    }

    @PostMapping("/select-rows")
    public List<ApiScenarioDTO> selectAllTableRows(@RequestBody TestPlanScenarioCaseBatchRequest request) {
        return testPlanScenarioCaseService.selectAllTableRows(request);
    }

    @PostMapping("/relevance/list/{goPage}/{pageSize}")
    public Pager<List<ApiScenarioDTO>> relevanceList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiScenarioRequest request) {
        return testPlanScenarioCaseService.relevanceList(request, goPage, pageSize);
    }

    @PostMapping("/relevance/projectIds")
    @MsRequestLog(module = OperLogModule.TRACK_TEST_PLAN)
    public ScenarioProjectDTO relevanceProjectIds(@RequestBody ApiScenarioRequest request) {
        return testPlanScenarioCaseService.relevanceProjectIds(request);
    }

    @PostMapping("/relevance")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.ASSOCIATE_CASE, content = "#msClass.getLogDetails(#request)", msClass = ApiScenarioService.class)
    public void testPlanRelevance(@RequestBody ApiCaseRelevanceRequest request) {
        testPlanScenarioCaseService.relevance(request);
    }

    @GetMapping("/delete/{id}")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE_REVIEW, type = OperLogConstants.UN_ASSOCIATE_CASE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = TestPlanScenarioCaseService.class)
    public int deleteTestCase(@PathVariable String id) {
        return testPlanScenarioCaseService.delete(id);
    }

    @PostMapping("/batch/delete")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.UN_ASSOCIATE_CASE, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = TestPlanScenarioCaseService.class)
    public void deleteApiCaseBath(@RequestBody TestPlanScenarioCaseBatchRequest request) {
        testPlanScenarioCaseService.deleteApiCaseBath(request);
    }

    @PostMapping(value = "/run")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.EXECUTE, content = "#msClass.getLogDetails(#request.planCaseIds)", msClass = TestPlanScenarioCaseService.class)
    public List<MsExecResponseDTO> run(@RequestBody RunTestPlanScenarioRequest request) {
        request.setExecuteType(ExecuteType.Completed.name());
        if (request.getConfig() == null) {
            RunModeConfigDTO config = new RunModeConfigDTO();
            config.setMode(RunModeConstants.PARALLEL.toString());
            config.setEnvMap(new HashMap<>());
            request.setConfig(config);
        }
        return testPlanScenarioCaseService.run(request);
    }

    @PostMapping(value = "/jenkins/run")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.EXECUTE, content = "#msClass.getLogDetails(#request.ids)", msClass = TestPlanScenarioCaseService.class)
    public List<MsExecResponseDTO> runByRun(@RequestBody RunTestPlanScenarioRequest request) {
        request.setExecuteType(ExecuteType.Saved.name());
        request.setTriggerMode(ApiRunMode.API.name());
        request.setRunMode(ApiRunMode.SCENARIO.name());
        return testPlanScenarioCaseService.run(request);
    }

    @PostMapping("/batch/update/env")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.BATCH_UPDATE, beforeEvent = "#msClass.batchLogDetails(#request.ids)", content = "#msClass.getLogDetails(#request.ids)", msClass = TestPlanScenarioCaseService.class)
    public void batchUpdateEnv(@RequestBody RelevanceScenarioRequest request) {
        testPlanScenarioCaseService.batchUpdateEnv(request);
    }

    @PostMapping("/env")
    public Map<String, String> getScenarioCaseEnv(@RequestBody HashMap<String, String> map) {
        return testPlanScenarioCaseService.getScenarioCaseEnv(map);
    }

    @PostMapping("/edit/order")
    @MsRequestLog(module = OperLogModule.TRACK_TEST_PLAN)
    public void orderCase(@RequestBody ResetOrderRequest request) {
        testPlanScenarioCaseService.updateOrder(request);
    }

    @PostMapping("/step/count")
    public TestPlanScenarioStepCountSimpleDTO getStepCount(@RequestBody List<PlanReportCaseDTO> planReportCaseDTOS) {
        return testPlanScenarioCaseService.getStepCount(planReportCaseDTOS);
    }

    @GetMapping("/plan/exec/result/{planId}")
    public List<String> getExecResultByPlanId(@PathVariable String planId) {
        return testPlanScenarioCaseService.getExecResultByPlanId(planId);
    }

    @PostMapping("/set/env/{planId}")
    public RunModeConfigDTO setScenarioEnv(@RequestBody RunModeConfigDTO runModeConfig, @PathVariable("planId") String planId) {
        return testPlanScenarioCaseService.setScenarioEnv(runModeConfig, planId);
    }

    @PostMapping("/relevance/{planId}")
    @MsRequestLog(module = OperLogModule.TRACK_TEST_PLAN)
    public void testPlanRelevance(@RequestBody List<String> ids, @PathVariable("planId") String planId) {
        testPlanScenarioCaseService.relevanceByTestIds(ids, planId);
    }

    @GetMapping("/plan/copy/{sourcePlanId}/{targetPlanId}")
    public void getStatusByTestPlanId(@PathVariable("sourcePlanId") String sourcePlanId, @PathVariable("targetPlanId") String targetPlanId) {
        testPlanScenarioCaseService.copyPlan(sourcePlanId, targetPlanId);
    }

    @GetMapping("/have/exec/{planId}")
    public Boolean haveExecCase(@PathVariable("planId") String planId) {
        return testPlanScenarioCaseService.haveExecCase(planId);
    }

    @PostMapping("/get/env")
    public Map<String, List<String>> getApiCaseEnv(@RequestBody List<String> planApiScenarioIds) {
        return testPlanScenarioCaseService.getApiScenarioEnv(planApiScenarioIds);
    }

    @GetMapping("/get/env/{planId}")
    public Map<String, List<String>> getApiCaseEnv(@PathVariable("planId") String planId) {
        return testPlanScenarioCaseService.getApiScenarioEnv(planId);
    }

    @GetMapping("/get/project/ids/{planId}")
    public List<String> getApiScenarioProjectIds(@PathVariable("planId") String planId) {
        return testPlanScenarioCaseService.getApiScenarioProjectIds(planId);
    }

    @PostMapping("/plan/report")
    public ApiPlanReportDTO buildApiReport(@RequestBody ApiPlanReportRequest request) {
        return testPlanScenarioCaseService.buildApiReport(request);
    }

    @PostMapping("/select/result/by/reportId")
    public ApiReportResultDTO selectReportResultById(@RequestBody ApiPlanReportRequest request) {
        return testPlanScenarioCaseService.buildExecuteApiReport(request);
    }

    @GetMapping("/is/executing/{planId}")
    public Boolean isExecuting(@PathVariable("planId") String planId) {
        return testPlanScenarioCaseService.isExecuting(planId);
    }

    @PostMapping("/all/list")
    public List<TestPlanScenarioDTO> getListByIds(@RequestBody Set<String> planApiCaseIds) {
        return testPlanScenarioCaseService.getListByIds(planApiCaseIds);
    }

    @PostMapping("/env/generate")
    public TestPlanEnvInfoDTO generateEnvironmentInfo(@RequestBody TestPlanReport testPlanReport) {
        return testPlanScenarioCaseService.generateEnvironmentInfo(testPlanReport);
    }

    @PostMapping("/has/fail/{planId}")
    public Boolean hasFailCase(@PathVariable("planId") String planId, @RequestBody List<String> automationIds) {
        return testPlanScenarioCaseService.hasFailCase(planId, automationIds);
    }

    @GetMapping("/get/report/status/{planId}")
    public List<PlanReportCaseDTO> selectStatusForPlanReport(@PathVariable("planId") String planId) {
        return testPlanScenarioCaseService.selectStatusForPlanReport(planId);
    }

    @PostMapping("/list/module/{planId}")
    public List<ApiScenarioModuleDTO> getNodeByPlanId(@PathVariable String planId, @RequestBody List<String> projectIds) {
        return testPlanScenarioCaseService.getNodeByPlanId(projectIds, planId);
    }

    @PostMapping("/build/response")
    public List<TestPlanScenarioDTO> buildResponse(@RequestBody List<TestPlanScenarioDTO> cases) {
        testPlanScenarioCaseService.buildScenarioResponse(cases);
        return cases;
    }

    @PostMapping("/get/plan/env/map")
    public AutomationsRunInfoDTO getPlanProjectEnvMap(@RequestBody List<String> resourceIds) {
        return testPlanScenarioCaseService.getPlanProjectEnvMap(resourceIds);
    }

    @GetMapping("/get-scenario-id/{id}")
    public String getScenarioId(@PathVariable("id") String planScenarioId) {
        return testPlanScenarioCaseService.getScenarioId(planScenarioId);
    }

}
