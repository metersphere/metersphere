package io.metersphere.track.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.automation.*;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.track.dto.RelevanceScenarioRequest;
import io.metersphere.track.request.testcase.TestPlanScenarioCaseBatchRequest;
import io.metersphere.track.service.TestPlanScenarioCaseService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public List<TestPlanFailureScenarioDTO> getFailureList(@PathVariable String planId) {
        return testPlanScenarioCaseService.getFailureCases(planId);
    }

    @GetMapping("/list/all/{planId}")
    public List<TestPlanFailureScenarioDTO> getAllList(@PathVariable String planId) {
        return testPlanScenarioCaseService.getAllCases(planId);
    }

    @PostMapping("/selectAllTableRows")
    public List<ApiScenarioDTO> selectAllTableRows(@RequestBody TestPlanScenarioCaseBatchRequest request) {
        return testPlanScenarioCaseService.selectAllTableRows(request);
    }

    @PostMapping("/relevance/list/{goPage}/{pageSize}")
    public Pager<List<ApiScenarioDTO>> relevanceList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiScenarioRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testPlanScenarioCaseService.relevanceList(request));
    }

    @GetMapping("/delete/{id}")
    @MsAuditLog(module = "track_test_case_review", type = OperLogConstants.UN_ASSOCIATE_CASE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = TestPlanScenarioCaseService.class)
    public int deleteTestCase(@PathVariable String id) {
        return testPlanScenarioCaseService.delete(id);
    }

    @PostMapping("/batch/delete")
    @MsAuditLog(module = "track_test_plan", type = OperLogConstants.UN_ASSOCIATE_CASE, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = TestPlanScenarioCaseService.class)
    public void deleteApiCaseBath(@RequestBody TestPlanScenarioCaseBatchRequest request) {
        testPlanScenarioCaseService.deleteApiCaseBath(request);
    }

    @PostMapping(value = "/run")
    @MsAuditLog(module = "track_test_plan", type = OperLogConstants.EXECUTE, content = "#msClass.getLogDetails(#request.planCaseIds)", msClass = TestPlanScenarioCaseService.class)
    public String run(@RequestBody RunTestPlanScenarioRequest request) {
        request.setExecuteType(ExecuteType.Completed.name());
        return testPlanScenarioCaseService.run(request);
    }

    @PostMapping(value = "/jenkins/run")
    @MsAuditLog(module = "track_test_plan", type = OperLogConstants.EXECUTE, content = "#msClass.getLogDetails(#request.ids)", msClass = TestPlanScenarioCaseService.class)
    public String runByRun(@RequestBody RunTestPlanScenarioRequest request) {
        request.setExecuteType(ExecuteType.Saved.name());
        request.setTriggerMode(ApiRunMode.API.name());
        request.setRunMode(ApiRunMode.SCENARIO.name());
        return testPlanScenarioCaseService.run(request);
    }

    @PostMapping("/batch/update/env")
    @MsAuditLog(module = "track_test_plan", type = OperLogConstants.BATCH_UPDATE, beforeEvent = "#msClass.batchLogDetails(#request.ids)", content = "#msClass.getLogDetails(#request.ids)", msClass = TestPlanScenarioCaseService.class)
    public void batchUpdateEnv(@RequestBody RelevanceScenarioRequest request) {
        testPlanScenarioCaseService.batchUpdateEnv(request);
    }

    @PostMapping("/env")
    public Map<String, String> getScenarioCaseEnv(@RequestBody HashMap<String, String> map) {
        return testPlanScenarioCaseService.getScenarioCaseEnv(map);
    }
}
