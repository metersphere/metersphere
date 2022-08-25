package io.metersphere.track.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.automation.ExecuteType;
import io.metersphere.api.dto.automation.RunTestPlanScenarioRequest;
import io.metersphere.api.dto.automation.TestPlanScenarioRequest;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.controller.request.ResetOrderRequest;
import io.metersphere.dto.*;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.track.dto.RelevanceScenarioRequest;
import io.metersphere.track.request.testcase.TestPlanScenarioCaseBatchRequest;
import io.metersphere.track.service.TestPlanScenarioCaseService;
import io.metersphere.track.service.TestPlanUiScenarioCaseService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/test/plan/uiScenario/case")
@RestController
public class TestPlanUiScenarioCaseController {

    @Resource
    TestPlanUiScenarioCaseService testPlanUiScenarioCaseService;

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<UiScenarioDTO>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody TestPlanScenarioRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testPlanUiScenarioCaseService.list(request));
    }

    @PostMapping("/list/all/{planId}")
    public List<TestPlanUiScenarioDTO> getAllList(@PathVariable String planId,
                                                  @RequestBody(required = false) List<String> statusList) {
        return testPlanUiScenarioCaseService.getAllCasesByStatusList(planId, statusList);
    }

    @PostMapping("/selectAllTableRows")
    public List<UiScenarioDTO> selectAllTableRows(@RequestBody TestPlanScenarioCaseBatchRequest request) {
        return testPlanUiScenarioCaseService.selectAllTableRows(request);
    }

    @PostMapping("/relevance/list/{goPage}/{pageSize}")
    public Pager<List<UiScenarioDTO>> relevanceList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody UiScenarioRequest request) {
        return testPlanUiScenarioCaseService.relevanceList(request, goPage, pageSize);
    }

    @PostMapping("/relevance/list/ids")
    public List<String> relevanceListIds(@RequestBody UiScenarioRequest request) {
        return testPlanUiScenarioCaseService.relevanceListIds(request);
    }

    @GetMapping("/delete/{id}")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE_REVIEW, type = OperLogConstants.UN_ASSOCIATE_CASE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = TestPlanScenarioCaseService.class)
    public int deleteTestCase(@PathVariable String id) {
        return testPlanUiScenarioCaseService.delete(id);
    }

    @PostMapping("/batch/delete")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.UN_ASSOCIATE_CASE, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = TestPlanScenarioCaseService.class)
    public void deleteApiCaseBath(@RequestBody TestPlanScenarioCaseBatchRequest request) {
        testPlanUiScenarioCaseService.deleteApiCaseBath(request);
    }

    @PostMapping(value = "/run")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.EXECUTE, content = "#msClass.getLogDetails(#request.planCaseIds)", msClass = TestPlanScenarioCaseService.class)
    public List<MsExecResponseDTO> run(@RequestBody RunTestPlanScenarioRequest request) {
        request.setExecuteType(ExecuteType.Completed.name());
        if(request.getConfig() == null){
            RunModeConfigDTO config = new RunModeConfigDTO();
            config.setMode(RunModeConstants.PARALLEL.toString());
            config.setEnvMap(new HashMap<>());
            request.setConfig(config);
        }
        return testPlanUiScenarioCaseService.run(request);
    }

    @PostMapping(value = "/jenkins/run")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.EXECUTE, content = "#msClass.getLogDetails(#request.ids)", msClass = TestPlanScenarioCaseService.class)
    public List<MsExecResponseDTO> runByRun(@RequestBody RunTestPlanScenarioRequest request) {
        request.setExecuteType(ExecuteType.Saved.name());
        request.setTriggerMode(ApiRunMode.API.name());
        request.setRunMode(ApiRunMode.SCENARIO.name());
        return testPlanUiScenarioCaseService.run(request);
    }

    @PostMapping("/batch/update/env")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.BATCH_UPDATE, beforeEvent = "#msClass.batchLogDetails(#request.ids)", content = "#msClass.getLogDetails(#request.ids)", msClass = TestPlanScenarioCaseService.class)
    public void batchUpdateEnv(@RequestBody RelevanceScenarioRequest request) {
        testPlanUiScenarioCaseService.batchUpdateEnv(request);
    }

    @PostMapping("/env")
    public Map<String, String> getScenarioCaseEnv(@RequestBody HashMap<String, String> map) {
        return testPlanUiScenarioCaseService.getScenarioCaseEnv(map);
    }

    @PostMapping("/edit/order")
    public void orderCase(@RequestBody ResetOrderRequest request) {
        testPlanUiScenarioCaseService.updateOrder(request);
    }
}
