package io.metersphere.track.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.automation.*;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.track.dto.RelevanceScenarioRequest;
import io.metersphere.track.request.testcase.TestPlanApiCaseBatchRequest;
import io.metersphere.track.service.TestCaseReviewScenarioCaseService;
import io.metersphere.track.service.TestPlanScenarioCaseService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/test/case/review/scenario/case")
public class TestCaseReviewScenarioCaseController {
    @Resource
    TestCaseReviewScenarioCaseService testCaseReviewScenarioCaseService;

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<ApiScenarioDTO>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody TestPlanScenarioRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testCaseReviewScenarioCaseService.list(request));
    }

    @PostMapping("/relevance/list/{goPage}/{pageSize}")
    public Pager<List<ApiScenarioDTO>> relevanceList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiScenarioRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testCaseReviewScenarioCaseService.relevanceList(request));
    }

    @GetMapping("/delete/{id}")
    public int deleteTestCase(@PathVariable String id) {
        return testCaseReviewScenarioCaseService.delete(id);
    }

    @PostMapping("/batch/delete")
    public void deleteApiCaseBath(@RequestBody TestPlanApiCaseBatchRequest request) {
        testCaseReviewScenarioCaseService.deleteApiCaseBath(request);
    }

    @PostMapping(value = "/run")
    public String run(@RequestBody RunScenarioRequest request) {
        request.setExecuteType(ExecuteType.Completed.name());
        return testCaseReviewScenarioCaseService.run(request);
    }

    @PostMapping("/batch/update/env")
    public void batchUpdateEnv(@RequestBody RelevanceScenarioRequest request) {
        testCaseReviewScenarioCaseService.batchUpdateEnv(request);
    }
}
