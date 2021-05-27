package io.metersphere.track.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.LoadTest;
import io.metersphere.base.domain.TestPlanLoadCase;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.performance.request.RunTestPlanRequest;
import io.metersphere.track.dto.TestPlanLoadCaseDTO;
import io.metersphere.track.request.testplan.LoadCaseReportBatchRequest;
import io.metersphere.track.request.testplan.LoadCaseReportRequest;
import io.metersphere.track.request.testplan.LoadCaseRequest;
import io.metersphere.track.request.testplan.RunBatchTestPlanRequest;
import io.metersphere.track.service.TestPlanLoadCaseService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("/test/plan/load/case")
@RestController
public class TestPlanLoadCaseController {

    @Resource
    private TestPlanLoadCaseService testPlanLoadCaseService;

    @PostMapping("/relevance/list/{goPage}/{pageSize}")
    public Pager<List<LoadTest>> relevanceList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody LoadCaseRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testPlanLoadCaseService.relevanceList(request));
    }

    @PostMapping("/relevance")
    @MsAuditLog(module = "track_test_plan", type = OperLogConstants.ASSOCIATE_CASE, content = "#msClass.getLogDetails(#request.caseIds,#request.testPlanId)", msClass = TestPlanLoadCaseService.class)
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
    @MsAuditLog(module = "track_test_plan", type = OperLogConstants.UN_ASSOCIATE_CASE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = TestPlanLoadCaseService.class)
    public void delete(@PathVariable String id) {
        testPlanLoadCaseService.delete(id);
    }

    @PostMapping("/run")
    @MsAuditLog(module = "track_test_plan", type = OperLogConstants.EXECUTE, content = "#msClass.getLogDetails(#request.testPlanLoadId)", msClass = TestPlanLoadCaseService.class)
    public String run(@RequestBody RunTestPlanRequest request) {
        return testPlanLoadCaseService.run(request);
    }

    @PostMapping("/run/batch")
    @MsAuditLog(module = "track_test_plan", type = OperLogConstants.EXECUTE, content = "#msClass.getRunLogDetails(#request.requests)", msClass = TestPlanLoadCaseService.class)
    public void runBatch(@RequestBody RunBatchTestPlanRequest request) {
        testPlanLoadCaseService.runBatch(request);
    }

    @PostMapping("/report/exist")
    public Boolean isExistReport(@RequestBody LoadCaseReportRequest request) {
        return testPlanLoadCaseService.isExistReport(request);
    }

    @PostMapping("/batch/delete")
    @MsAuditLog(module = "track_test_plan", type = OperLogConstants.UN_ASSOCIATE_CASE, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = TestPlanLoadCaseService.class)
    public void batchDelete(@RequestBody LoadCaseReportBatchRequest request) {
        testPlanLoadCaseService.batchDelete(request);
    }

    @PostMapping("/update")
    @MsAuditLog(module = "track_test_plan", type = OperLogConstants.UPDATE,  content = "#msClass.getLogDetails(#testPlanLoadCase.id)", msClass = TestPlanLoadCaseService.class)
    public void update(@RequestBody TestPlanLoadCase testPlanLoadCase) {
        testPlanLoadCaseService.update(testPlanLoadCase);
    }

    @PostMapping("/update/api")
    @MsAuditLog(module = "track_test_plan", type = OperLogConstants.UPDATE,  content = "#msClass.getLogDetails(#testPlanLoadCase.id)", msClass = TestPlanLoadCaseService.class)
    public void updateByApi(@RequestBody TestPlanLoadCase testPlanLoadCase) {
        testPlanLoadCaseService.updateByApi(testPlanLoadCase);
    }
}
