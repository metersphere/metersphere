package io.metersphere.track.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.LoadTest;
import io.metersphere.base.domain.TestPlanLoadCase;
import io.metersphere.base.domain.TestPlanLoadCaseWithBLOBs;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.constants.TriggerMode;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.controller.request.ResetOrderRequest;
import io.metersphere.dto.LoadTestDTO;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.performance.request.RunTestPlanRequest;
import io.metersphere.track.dto.TestPlanLoadCaseDTO;
import io.metersphere.track.request.testplan.LoadCaseReportBatchRequest;
import io.metersphere.track.request.testplan.LoadCaseReportRequest;
import io.metersphere.track.request.testplan.LoadCaseRequest;
import io.metersphere.track.request.testplan.RunBatchTestPlanRequest;
import io.metersphere.track.service.TestPlanLoadCaseService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("/test/plan/load/case")
@RestController
public class TestPlanLoadCaseController {

    @Resource
    private TestPlanLoadCaseService testPlanLoadCaseService;

    @PostMapping("/relevance/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public Pager<List<LoadTestDTO>> relevanceList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody LoadCaseRequest request) {
        return testPlanLoadCaseService.relevanceList(request, goPage, pageSize);
    }

    @PostMapping("/relevance")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_RELEVANCE_OR_CANCEL)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.ASSOCIATE_CASE, content = "#msClass.getLogDetails(#request.caseIds,#request.testPlanId)", msClass = TestPlanLoadCaseService.class)
    public void relevanceCase(@RequestBody LoadCaseRequest request) {
        testPlanLoadCaseService.relevanceCase(request);
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public Pager<List<TestPlanLoadCaseDTO>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody LoadCaseRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testPlanLoadCaseService.list(request));
    }

    @PostMapping("/selectAllTableRows")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public List<TestPlanLoadCaseDTO> selectAllTableRows(@RequestBody LoadCaseReportBatchRequest request) {
        return testPlanLoadCaseService.selectAllTableRows(request);
    }

    @GetMapping("/delete/{id}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_CREATE)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.UN_ASSOCIATE_CASE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = TestPlanLoadCaseService.class)
    public void delete(@PathVariable String id) {
        testPlanLoadCaseService.delete(id);
    }

    @PostMapping("/run")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_RUN)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.EXECUTE, content = "#msClass.getLogDetails(#request.testPlanLoadId)", msClass = TestPlanLoadCaseService.class)
    public String run(@RequestBody RunTestPlanRequest request) {
        return testPlanLoadCaseService.run(request);
    }

    @PostMapping("/run/batch")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_RUN)
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
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public Boolean isExistReport(@RequestBody LoadCaseReportRequest request) {
        return testPlanLoadCaseService.isExistReport(request);
    }

    @PostMapping("/batch/delete")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_RELEVANCE_OR_CANCEL)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.UN_ASSOCIATE_CASE, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = TestPlanLoadCaseService.class)
    public void batchDelete(@RequestBody LoadCaseReportBatchRequest request) {
        testPlanLoadCaseService.batchDelete(request);
    }

    @PostMapping("/update")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_RUN)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.UPDATE, content = "#msClass.getLogDetails(#testPlanLoadCase.id)", msClass = TestPlanLoadCaseService.class)
    public void update(@RequestBody TestPlanLoadCaseWithBLOBs testPlanLoadCase) {
        testPlanLoadCaseService.update(testPlanLoadCase);
    }

    @PostMapping("/update/api")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_RUN)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.UPDATE, content = "#msClass.getLogDetails(#testPlanLoadCase.id)", msClass = TestPlanLoadCaseService.class)
    public void updateByApi(@RequestBody TestPlanLoadCase testPlanLoadCase) {
        testPlanLoadCaseService.updateByApi(testPlanLoadCase);
    }

    @GetMapping("/list/failure/{planId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public List<TestPlanLoadCaseDTO> getFailureCases(@PathVariable String planId) {
        return testPlanLoadCaseService.getFailureCases(planId);
    }

    @GetMapping("/list/all/{planId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public List<TestPlanLoadCaseDTO> getAllCases(@PathVariable String planId) {
        return testPlanLoadCaseService.getAllCases(planId);
    }

    @GetMapping("/get-load-config/{loadCaseId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public String getPlanLoadCaseConfig(@PathVariable String loadCaseId) {
        return testPlanLoadCaseService.getPlanLoadCaseConfig(loadCaseId);
    }

    @GetMapping("/get-advanced-config/{loadCaseId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public String getAdvancedConfiguration(@PathVariable String loadCaseId) {
        return testPlanLoadCaseService.getAdvancedConfiguration(loadCaseId);
    }

    @GetMapping("/get/{loadCaseId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public TestPlanLoadCase getTestPlanLoadCase(@PathVariable String loadCaseId) {
        return testPlanLoadCaseService.getTestPlanLoadCase(loadCaseId);
    }

    @PostMapping("/edit/order")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_RELEVANCE_OR_CANCEL)
    public void orderCase(@RequestBody ResetOrderRequest request) {
        testPlanLoadCaseService.updateOrder(request);
    }
}
