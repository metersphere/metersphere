package io.metersphere.controller.plan;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.QueryReferenceRequest;
import io.metersphere.api.dto.automation.TestPlanApiDTO;
import io.metersphere.api.dto.automation.TestPlanDTO;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.plan.AutomationsRunInfoDTO;
import io.metersphere.api.dto.plan.TestPlanApiCaseBatchRequest;
import io.metersphere.base.domain.ApiDefinitionExecResultWithBLOBs;
import io.metersphere.base.domain.ApiScenarioReportWithBLOBs;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.log.annotation.MsRequestLog;
import io.metersphere.request.ResetOrderRequest;
import io.metersphere.service.plan.TestPlanApiCaseService;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RequestMapping("/test/plan/api/case")
@RestController
public class TestPlanApiCaseController {

    @Resource
    TestPlanApiCaseService testPlanApiCaseService;

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<TestPlanApiCaseDTO>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiTestCaseRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testPlanApiCaseService.list(request));
    }

    @GetMapping("/list/failure/{planId}")
    public List<TestPlanApiDTO> getFailureList(@PathVariable String planId) {
        return testPlanApiCaseService.getFailureCases(planId);
    }

    @GetMapping("/list/errorReport/{planId}")
    public List<TestPlanApiDTO> getErrorReportList(@PathVariable String planId) {
        return testPlanApiCaseService.getErrorReportCases(planId);
    }

    @GetMapping("/list/unExecute/{planId}")
    public List<TestPlanApiDTO> getUnExecuteCases(@PathVariable String planId) {
        return testPlanApiCaseService.getUnExecuteCases(planId);
    }

    @GetMapping("/list/all/{planId}")
    public List<TestPlanApiDTO> getAllList(@PathVariable String planId) {
        return testPlanApiCaseService.getAllCases(planId);
    }

    @GetMapping("/list/{planId}")
    public List<TestPlanApiCaseDTO> getByPlanId(@PathVariable String planId) {
        ApiTestCaseRequest request = new ApiTestCaseRequest();
        request.setPlanId(planId);
        return testPlanApiCaseService.list(request);
    }

    @GetMapping("/plan/exec/result/{planId}")
    public List<String> getExecResultByPlanId(@PathVariable String planId) {
        return testPlanApiCaseService.getExecResultByPlanId(planId);
    }

    @PostMapping("/select-rows")
    public List<TestPlanApiCaseDTO> selectAllTableRows(@RequestBody TestPlanApiCaseBatchRequest request) {
        return testPlanApiCaseService.selectAllTableRows(request);
    }

    @PostMapping("/relevance/list/{goPage}/{pageSize}")
    public Pager<List<ApiTestCaseDTO>> relevanceList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiTestCaseRequest request) {
        return testPlanApiCaseService.relevanceList(goPage, pageSize, request);
    }

    @PostMapping("/relevance/{planId}")
    @MsRequestLog(module = OperLogModule.TRACK_TEST_PLAN)
    public void testPlanRelevance(@RequestBody List<String> ids, @PathVariable("planId") String planId) {
        testPlanApiCaseService.relevanceByTestIds(ids, planId);
    }

    @GetMapping("/status/{planId}")
    public List<String> getStatusByTestPlanId(@PathVariable("planId") String planId) {
        return testPlanApiCaseService.getStatusByTestPlanId(planId);
    }

    @GetMapping("/plan/copy/{sourcePlanId}/{targetPlanId}")
    @MsRequestLog(module = OperLogModule.TRACK_TEST_PLAN)
    public void getStatusByTestPlanId(@PathVariable("sourcePlanId") String sourcePlanId, @PathVariable("targetPlanId") String targetPlanId) {
        testPlanApiCaseService.copyPlan(sourcePlanId, targetPlanId);
    }

    @GetMapping("/have/exec/{planId}")
    public boolean haveExecCase(@PathVariable("planId") String planId) {
        return testPlanApiCaseService.haveExecCase(planId);
    }

    @GetMapping("/delete/{id}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_RELEVANCE_OR_CANCEL)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE_REVIEW, type = OperLogConstants.UN_ASSOCIATE_CASE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = TestPlanApiCaseService.class)
    public int deleteTestCase(@PathVariable String id) {
        return testPlanApiCaseService.delete(id);
    }

    @PostMapping("/batch/delete")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_RELEVANCE_OR_CANCEL)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.UN_ASSOCIATE_CASE, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = TestPlanApiCaseService.class)
    public void deleteApiCaseBath(@RequestBody TestPlanApiCaseBatchRequest request) {
        testPlanApiCaseService.deleteApiCaseBath(request);
    }

    @PostMapping("/batch/update/env")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_RELEVANCE_OR_CANCEL)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.BATCH_UPDATE, beforeEvent = "#msClass.batchLogDetails(#request.ids)", content = "#msClass.getLogDetails(#request.ids)", msClass = TestPlanApiCaseService.class)
    public void batchUpdateEnv(@RequestBody TestPlanApiCaseBatchRequest request) {
        testPlanApiCaseService.batchUpdateEnv(request);
    }

    @PostMapping(value = "/run")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.EXECUTE, content = "#msClass.getLogDetails(#request.planIds)", msClass = TestPlanApiCaseService.class)
    public List<MsExecResponseDTO> run(@RequestBody BatchRunDefinitionRequest request) {
        return testPlanApiCaseService.run(request);
    }

    @PostMapping("/edit/order")
    @MsRequestLog(module = OperLogModule.TRACK_TEST_PLAN)
    public void orderCase(@RequestBody ResetOrderRequest request) {
        testPlanApiCaseService.updateOrder(request);
    }

    @GetMapping("/get/report/status/{planId}")
    public List<Map> selectStatusForPlanReport(@PathVariable("planId") String planId) {
        return testPlanApiCaseService.selectStatusForPlanReport(planId);
    }

    @PostMapping("/get/plan/env/map")
    public AutomationsRunInfoDTO getPlanProjectEnvMap(@RequestBody List<String> resourceIds) {
        return testPlanApiCaseService.getPlanProjectEnvMap(resourceIds);
    }

    @PostMapping("/set/env/{planId}")
    public RunModeConfigDTO setApiCaseEnv(@RequestBody RunModeConfigDTO runModeConfig, @PathVariable("planId") String planId) {
        return testPlanApiCaseService.setApiCaseEnv(runModeConfig, planId);
    }

    @PostMapping("/get/env")
    public Map<String, List<String>> getApiCaseEnv(@RequestBody List<String> planApiCaseIds) {
        return testPlanApiCaseService.getApiCaseEnv(planApiCaseIds);
    }

    @GetMapping("/get/env/{planId}")
    public Map<String, List<String>> getApiCaseEnv(@PathVariable("planId") String planId) {
        return testPlanApiCaseService.getApiCaseEnv(planId);
    }

    @GetMapping("/get/project/ids/{planId}")
    public List<String> getApiCaseProjectIds(@PathVariable("planId") String planId) {
        return testPlanApiCaseService.getApiCaseProjectIds(planId);
    }

    @GetMapping("/is/executing/{planId}")
    public Boolean isExecuting(@PathVariable("planId") String planId) {
        return testPlanApiCaseService.isExecuting(planId);
    }

    @PostMapping("/failure/list")
    public List<TestPlanApiDTO> getFailureListByIds(@RequestBody Set<String> planApiCaseIds) {
        return testPlanApiCaseService.getFailureListByIds(planApiCaseIds);
    }

    @PostMapping("/has/fail/{planId}")
    public Boolean hasFailCase(@PathVariable("planId") String planId, @RequestBody List<String> planApiCaseIds) {
        return testPlanApiCaseService.hasFailCase(planId, planApiCaseIds);
    }

    @PostMapping("/list/module/{planId}/{protocol}")
    public List<ApiModuleDTO> getNodeByPlanId(@PathVariable String planId, @PathVariable String protocol, @RequestBody List<String> projectIds) {
        return testPlanApiCaseService.getNodeByPlanId(projectIds, planId, protocol);
    }

    @GetMapping("/run/{testId}/{reportId}")
    @MsRequestLog(module = OperLogModule.TRACK_TEST_PLAN)
    public void runApi(@PathVariable String testId, @PathVariable String reportId) {
        testPlanApiCaseService.run(testId, reportId);
    }

    @PostMapping("/build/response")
    public List<TestPlanApiDTO> buildResponse(@RequestBody List<TestPlanApiDTO> cases) {
        testPlanApiCaseService.buildApiResponse(cases);
        return cases;
    }

    @PostMapping("/get-reference/{goPage}/{pageSize}")
    public Pager<List<TestPlanDTO>> getReference(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryReferenceRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testPlanApiCaseService.getReference(request));
    }

    @GetMapping("/get/report/ext/{planId}")
    public List<ApiDefinitionExecResultWithBLOBs> selectExtForPlanReport(@PathVariable("planId") String planId) {
        return testPlanApiCaseService.selectExtForPlanReport(planId);
    }

    @GetMapping("/get/report/scenario/ext/{planId}")
    public List<ApiScenarioReportWithBLOBs> selectExtForPlanScenarioReport(@PathVariable("planId") String planId) {
        return testPlanApiCaseService.selectExtForPlanScenarioReport(planId);
    }

}
