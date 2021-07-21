package io.metersphere.track.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.TestPlan;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.service.CheckPermissionService;
import io.metersphere.track.dto.ApiRunConfigDTO;
import io.metersphere.track.dto.TestCaseReportMetricDTO;
import io.metersphere.track.dto.TestPlanDTO;
import io.metersphere.track.dto.TestPlanDTOWithMetric;
import io.metersphere.track.request.testcase.PlanCaseRelevanceRequest;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import io.metersphere.track.request.testplan.AddTestPlanRequest;
import io.metersphere.track.request.testplan.TestplanRunRequest;
import io.metersphere.track.request.testplancase.TestCaseRelevanceRequest;
import io.metersphere.track.service.TestPlanProjectService;
import io.metersphere.track.service.TestPlanService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@RequestMapping("/test/plan")
@RestController
public class TestPlanController {

    @Resource
    TestPlanService testPlanService;
    @Resource
    TestPlanProjectService testPlanProjectService;
    @Resource
    CheckPermissionService checkPermissionService;

    @PostMapping("/autoCheck/{testPlanId}")
    public void autoCheck(@PathVariable String testPlanId){
        testPlanService.checkStatus(testPlanId);
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    @RequiresPermissions("PROJECT_TRACK_PLAN:READ")
    public Pager<List<TestPlanDTOWithMetric>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestPlanRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testPlanService.listTestPlan(request));
    }

    /*jenkins测试计划*/
    @GetMapping("/list/all/{projectId}/{workspaceId}")
    public List<TestPlanDTOWithMetric> listByProjectId(@PathVariable String projectId, @PathVariable String workspaceId) {
        QueryTestPlanRequest request = new QueryTestPlanRequest();
        request.setWorkspaceId(workspaceId);
        request.setProjectId(projectId);
        return testPlanService.listTestPlanByProject(request);
    }

    @PostMapping("/list/all")
    public List<TestPlan> listAll(@RequestBody QueryTestPlanRequest request) {
        return testPlanService.listTestAllPlan(request);
    }

    @PostMapping("/list/all/relate")
    public List<TestPlanDTOWithMetric> listRelateAll() {
        return testPlanService.listRelateAllPlan();
    }

    @GetMapping("recent/{count}/{id}")
    public List<TestPlan> recentTestPlans(@PathVariable("count") int count, @PathVariable("id") String projectId) {
        PageHelper.startPage(1, count, true);
        return testPlanService.recentTestPlans(projectId);
    }

    @PostMapping("/get/{testPlanId}")
    public TestPlan getTestPlan(@PathVariable String testPlanId) {
        checkPermissionService.checkTestPlanOwner(testPlanId);
        return testPlanService.getTestPlan(testPlanId);
    }

    @PostMapping("/add")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_CREATE)
    @MsAuditLog(module = "track_test_plan", type = OperLogConstants.CREATE, title = "#testPlan.name", content = "#msClass.getLogDetails(#testPlan.id)", msClass = TestPlanService.class)
    public String addTestPlan(@RequestBody AddTestPlanRequest testPlan) {
        testPlan.setId(UUID.randomUUID().toString());
        return testPlanService.addTestPlan(testPlan);

    }

    @PostMapping("/edit")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_EDIT)
    @MsAuditLog(module = "track_test_plan", type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#testPlanDTO.id)", content = "#msClass.getLogDetails(#testPlanDTO.id)", msClass = TestPlanService.class)
    public String editTestPlan(@RequestBody TestPlanDTO testPlanDTO) {
        return testPlanService.editTestPlan(testPlanDTO, true);
    }

    @PostMapping("/edit/status/{planId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_EDIT)
    @MsAuditLog(module = "track_test_plan", type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#planId)", content = "#msClass.getLogDetails(#planId)", msClass = TestPlanService.class)
    public void editTestPlanStatus(@PathVariable String planId) {
        checkPermissionService.checkTestPlanOwner(planId);
        testPlanService.editTestPlanStatus(planId);
    }

    @PostMapping("/delete/{testPlanId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_DELETE)
    @MsAuditLog(module = "track_test_plan", type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#testPlanId)", msClass = TestPlanService.class)
    public int deleteTestPlan(@PathVariable String testPlanId) {
        checkPermissionService.checkTestPlanOwner(testPlanId);
        return testPlanService.deleteTestPlan(testPlanId);
    }

    @PostMapping("/relevance")
    @MsAuditLog(module = "track_test_plan", type = OperLogConstants.ASSOCIATE_CASE, content = "#msClass.getLogDetails(#request)", msClass = TestPlanService.class)
    public void testPlanRelevance(@RequestBody PlanCaseRelevanceRequest request) {
        testPlanService.testPlanRelevance(request);
    }

    @GetMapping("/get/metric/{planId}")
    public TestCaseReportMetricDTO getMetric(@PathVariable String planId) {
        return testPlanService.getMetric(planId);
    }

    @GetMapping("/get/statistics/metric/{planId}")
    public TestCaseReportMetricDTO getStatisticsMetric(@PathVariable String planId) {
        return testPlanService.getStatisticsMetric(planId);
    }

    @GetMapping("/project/name/{planId}")
    public String getProjectNameByPlanId(@PathVariable String planId) {
        return testPlanService.getProjectNameByPlanId(planId);
    }

    @PostMapping("/project")
    public List<Project> getProjectByPlanId(@RequestBody TestCaseRelevanceRequest request) {
        List<String> projectIds = testPlanProjectService.getProjectIdsByPlanId(request.getPlanId());
        request.setProjectIds(projectIds);
        return testPlanProjectService.getProjectByPlanId(request);
    }

    @PostMapping("/project/{goPage}/{pageSize}")
    public Pager<List<Project>> getProjectByPlanId(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody TestCaseRelevanceRequest request) {
        List<String> projectIds = testPlanProjectService.getProjectIdsByPlanId(request.getPlanId());
        request.setProjectIds(projectIds);
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testPlanProjectService.getProjectByPlanId(request));
    }

    @PostMapping("/testplan/jenkins")
    public String runJenkins(@RequestBody TestplanRunRequest testplanRunRequest) {
        ApiRunConfigDTO api = new ApiRunConfigDTO();
        api.setMode(testplanRunRequest.getMode());
        api.setResourcePoolId(testplanRunRequest.getResourcePoolId());
        api.setOnSampleError(true);
        api.setReportType("iddReport");
        String apiRunConfig = JSONObject.toJSONString(api);
        return testPlanService.run(testplanRunRequest.getTestPlanId(), testplanRunRequest.getProjectId(), testplanRunRequest.getUserId(), testplanRunRequest.getTriggerMode(), apiRunConfig);
    }

}
