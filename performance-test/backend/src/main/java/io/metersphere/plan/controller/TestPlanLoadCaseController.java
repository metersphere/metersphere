package io.metersphere.plan.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.TestPlanLoadCase;
import io.metersphere.base.domain.TestPlanLoadCaseWithBLOBs;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.TriggerMode;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.dto.LoadTestDTO;
import io.metersphere.dto.PlanReportCaseDTO;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.plan.dto.LoadPlanReportDTO;
import io.metersphere.plan.dto.TestPlanLoadCaseDTO;
import io.metersphere.plan.request.LoadCaseReportBatchRequest;
import io.metersphere.plan.request.LoadCaseReportRequest;
import io.metersphere.plan.request.LoadCaseRequest;
import io.metersphere.plan.request.RunBatchTestPlanRequest;
import io.metersphere.plan.service.TestPlanLoadCaseService;
import io.metersphere.request.PlanSubReportRequest;
import io.metersphere.request.ResetOrderRequest;
import io.metersphere.request.RunTestPlanRequest;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("/test/plan/load/case")
@RestController
public class TestPlanLoadCaseController {

    @Resource
    private TestPlanLoadCaseService testPlanLoadCaseService;

    @PostMapping("/relevance/list/{goPage}/{pageSize}")
    public Pager<List<LoadTestDTO>> relevanceList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody LoadCaseRequest request) {
        return testPlanLoadCaseService.relevanceList(request, goPage, pageSize);
    }

    @PostMapping("/relevance")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.ASSOCIATE_CASE, content = "#msClass.getLogDetails(#request.caseIds,#request.testPlanId)", msClass = TestPlanLoadCaseService.class)
    public void relevanceCase(@RequestBody LoadCaseRequest request) {
        testPlanLoadCaseService.relevanceCase(request);
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<TestPlanLoadCaseDTO>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody LoadCaseRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testPlanLoadCaseService.list(request));
    }

    @PostMapping("/selectAllTableRows")
    public List<TestPlanLoadCaseDTO> selectAllTableRows(@RequestBody LoadCaseReportBatchRequest request) {
        return testPlanLoadCaseService.selectAllTableRows(request);
    }

    @GetMapping("/delete/{id}")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.UN_ASSOCIATE_CASE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = TestPlanLoadCaseService.class)
    public void delete(@PathVariable String id) {
        testPlanLoadCaseService.delete(id);
    }

    @PostMapping("/run")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.EXECUTE, content = "#msClass.getLogDetails(#request.testPlanLoadId)", msClass = TestPlanLoadCaseService.class)
    public String run(@RequestBody RunTestPlanRequest request) {
        return testPlanLoadCaseService.run(request);
    }

    @PostMapping("/run/batch")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.EXECUTE, content = "#msClass.getRunLogDetails(#request.requests)", msClass = TestPlanLoadCaseService.class)
    public void runBatch(@RequestBody RunBatchTestPlanRequest request) {
        if (request.getRequests() != null) {
            for (RunTestPlanRequest req : request.getRequests()) {
                req.setTriggerMode(TriggerMode.BATCH.name());
            }
        }
        testPlanLoadCaseService.runBatch(request);
    }

    @PostMapping("/report/exist")
    public Boolean isExistReport(@RequestBody LoadCaseReportRequest request) {
        return testPlanLoadCaseService.isExistReport(request);
    }

    @PostMapping("/batch/delete")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.UN_ASSOCIATE_CASE, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = TestPlanLoadCaseService.class)
    public void batchDelete(@RequestBody LoadCaseReportBatchRequest request) {
        testPlanLoadCaseService.batchDelete(request);
    }

    @PostMapping("/update")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.UPDATE, content = "#msClass.getLogDetails(#testPlanLoadCase.id)", msClass = TestPlanLoadCaseService.class)
    public void update(@RequestBody TestPlanLoadCaseWithBLOBs testPlanLoadCase) {
        testPlanLoadCaseService.update(testPlanLoadCase);
    }

    @PostMapping("/update/api")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.UPDATE, content = "#msClass.getLogDetails(#testPlanLoadCase.id)", msClass = TestPlanLoadCaseService.class)
    public void updateByApi(@RequestBody TestPlanLoadCase testPlanLoadCase) {
        testPlanLoadCaseService.updateByApi(testPlanLoadCase);
    }

    @GetMapping("/list/failure/{planId}")
    public List<TestPlanLoadCaseDTO> getFailureCases(@PathVariable String planId) {
        return testPlanLoadCaseService.getFailureCases(planId);
    }

    @GetMapping("/list/all/{planId}")
    public List<TestPlanLoadCaseDTO> getAllCases(@PathVariable String planId) {
        return testPlanLoadCaseService.getAllCases(planId);
    }

    @GetMapping("/get-load-config/{loadCaseId}")
    public String getPlanLoadCaseConfig(@PathVariable String loadCaseId) {
        return testPlanLoadCaseService.getPlanLoadCaseConfig(loadCaseId);
    }

    @GetMapping("/get-advanced-config/{loadCaseId}")
    public String getAdvancedConfiguration(@PathVariable String loadCaseId) {
        return testPlanLoadCaseService.getAdvancedConfiguration(loadCaseId);
    }

    @GetMapping("/get/{loadCaseId}")
    public TestPlanLoadCase getTestPlanLoadCase(@PathVariable String loadCaseId) {
        return testPlanLoadCaseService.getTestPlanLoadCase(loadCaseId);
    }

    @PostMapping("/edit/order")
    public void orderCase(@RequestBody ResetOrderRequest request) {
        testPlanLoadCaseService.updateOrder(request);
    }

    @GetMapping("/plan/status/{planId}")
    public List<String> getStatusByTestPlanId(@PathVariable String planId) {
        return testPlanLoadCaseService.getStatusByTestPlanId(planId);
    }

    @GetMapping("/list/{planId}")
    public List<TestPlanLoadCaseDTO> getByPlanId(@PathVariable String planId) {
        LoadCaseRequest request = new LoadCaseRequest();
        request.setTestPlanId(planId);
        return testPlanLoadCaseService.list(request);
    }

    @GetMapping("/list")
    public List<TestPlanLoadCaseDTO> getByPlanId(LoadCaseRequest request) {
        return testPlanLoadCaseService.list(request);
    }

    @GetMapping("/plan/copy/{sourcePlanId}/{targetPlanId}")
    public void getStatusByTestPlanId(@PathVariable("sourcePlanId") String sourcePlanId, @PathVariable("targetPlanId") String targetPlanId) {
        testPlanLoadCaseService.copyPlan(sourcePlanId, targetPlanId);
    }

    @GetMapping("/plan/report")
    public LoadPlanReportDTO buildApiReport(@RequestBody PlanSubReportRequest request) {
        return testPlanLoadCaseService.buildLoadReport(request);
    }

    @PostMapping("/plan/execute/report")
    public LoadPlanReportDTO buildExecuteApiReport(@RequestBody PlanSubReportRequest request) {
        return testPlanLoadCaseService.buildExecuteLoadReport(request);
    }

    @GetMapping("/have/exec/{planId}")
    public Boolean haveExecCase(@PathVariable("planId") String planId) {
        return testPlanLoadCaseService.haveExecCase(planId);
    }

    @PostMapping("/relevance/{planId}")
    public void testPlanRelevance(@RequestBody List<String> ids, @PathVariable("planId") String planId) {
        testPlanLoadCaseService.relevanceByTestIds(ids, planId);
    }

    @GetMapping("/plan/exec/result/{planId}")
    public List<String> getExecResultByPlanId(@PathVariable String planId) {
        return testPlanLoadCaseService.getStatus(planId);
    }

    @GetMapping("/get/report/status/{planId}")
    public List<PlanReportCaseDTO> selectStatusForPlanReport(@PathVariable("planId") String planId) {
        return testPlanLoadCaseService.selectStatusForPlanReport(planId);
    }

    @PostMapping("/has/fail/{planId}")
    public Boolean hasFailCase(@PathVariable("planId") String planId, @RequestBody List<String> planApiCaseIds) {
        return testPlanLoadCaseService.hasFailCase(planId, planApiCaseIds);
    }

    @GetMapping("/is/executing/{planId}/{projectId}")
    public Boolean isExecuting(@PathVariable("planId") String planId, @PathVariable("projectId") String projectId) {
        return testPlanLoadCaseService.isExecuting(planId, projectId);
    }


}
