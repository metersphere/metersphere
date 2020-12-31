package io.metersphere.track.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.automation.*;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.track.request.testcase.TestPlanApiCaseBatchRequest;
import io.metersphere.track.service.TestPlanScenarioCaseService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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

    @PostMapping("/relevance/list/{goPage}/{pageSize}")
    public Pager<List<ApiScenarioDTO>> relevanceList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiScenarioRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        return PageUtils.setPageInfo(page, testPlanScenarioCaseService.relevanceList(request));
    }

    @GetMapping("/delete/{id}")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public int deleteTestCase(@PathVariable String id) {
        return testPlanScenarioCaseService.delete(id);
    }

    @PostMapping("/batch/delete")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public void deleteApiCaseBath(@RequestBody TestPlanApiCaseBatchRequest request) {
        testPlanScenarioCaseService.deleteApiCaseBath(request);
    }

    @PostMapping(value = "/run")
    public String run(@RequestBody RunScenarioRequest request) {
        request.setExecuteType(ExecuteType.Completed.name());
        return testPlanScenarioCaseService.run(request);
    }
}
