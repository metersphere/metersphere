package io.metersphere.track.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.datacount.request.ScheduleInfoRequest;
import io.metersphere.api.service.ApiAutomationService;
import io.metersphere.base.domain.*;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.notice.annotation.SendNotice;
import io.metersphere.service.CheckPermissionService;
import io.metersphere.service.ScheduleService;
import io.metersphere.track.dto.*;
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
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
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
    @Resource
    private ScheduleService scheduleService;
    @Resource
    private ApiAutomationService apiAutomationService;

    @PostMapping("/autoCheck/{testPlanId}")
    public void autoCheck(@PathVariable String testPlanId) {
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
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public List<TestPlan> listAll(@RequestBody QueryTestPlanRequest request) {
        return testPlanService.listTestAllPlan(request);
    }

    @GetMapping("/get/stage/option/{projectId}")
    public JSONArray getStageOption(@PathVariable("projectId") String projectId) {
        return testPlanService.getStageOption(projectId);
    }

    @GetMapping("recent/{count}/{id}")
    public List<TestPlan> recentTestPlans(@PathVariable("count") int count, @PathVariable("id") String projectId) {
        PageHelper.startPage(1, count, true);
        return testPlanService.recentTestPlans(projectId);
    }

    @PostMapping("/get/{testPlanId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public TestPlan getTestPlan(@PathVariable String testPlanId) {
        checkPermissionService.checkTestPlanOwner(testPlanId);
        return testPlanService.getTestPlan(testPlanId);
    }

    @PostMapping("/add")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_CREATE)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.CREATE, title = "#testPlan.name", content = "#msClass.getLogDetails(#testPlan.id)", msClass = TestPlanService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.TEST_PLAN_TASK, event = NoticeConstants.Event.CREATE, mailTemplate = "track/TestPlanStart", subject = "测试计划通知")
    public TestPlan addTestPlan(@RequestBody AddTestPlanRequest testPlan) {
        testPlan.setId(UUID.randomUUID().toString());
        return testPlanService.addTestPlan(testPlan);
    }

    @PostMapping("/edit")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_EDIT)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#testPlanDTO.id)", content = "#msClass.getLogDetails(#testPlanDTO.id)", msClass = TestPlanService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.TEST_PLAN_TASK, event = NoticeConstants.Event.UPDATE, mailTemplate = "track/TestPlanUpdate", subject = "测试计划通知")
    public TestPlan editTestPlan(@RequestBody AddTestPlanRequest testPlanDTO) {
        return testPlanService.editTestPlanWithRequest(testPlanDTO);
    }

    @PostMapping("/edit/status/{planId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_EDIT)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#planId)", content = "#msClass.getLogDetails(#planId)", msClass = TestPlanService.class)
    public void editTestPlanStatus(@PathVariable String planId) {
        checkPermissionService.checkTestPlanOwner(planId);
        testPlanService.editTestPlanStatus(planId);
    }

    @PostMapping("/edit/report/config")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_EDIT)
//    @MsAuditLog(module = "track_test_plan", type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#planId)", content = "#msClass.getLogDetails(#planId)", msClass = TestPlanService.class)
    public void editReportConfig(@RequestBody TestPlanDTO testPlanDTO) {
        testPlanService.editReportConfig(testPlanDTO);
    }

    @PostMapping("/edit/follows/{planId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_EDIT)
    public void editTestFollows(@PathVariable String planId, @RequestBody List<String> follows) {
        testPlanService.editTestFollows(planId, follows);
    }

    @PostMapping("/delete/{testPlanId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_DELETE)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#testPlanId)", msClass = TestPlanService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.TEST_PLAN_TASK, target = "#targetClass.get(#testPlanId)", targetClass = TestPlanService.class,
            event = NoticeConstants.Event.DELETE, mailTemplate = "track/TestPlanDelete", subject = "测试计划通知")
    public int deleteTestPlan(@PathVariable String testPlanId) {
        checkPermissionService.checkTestPlanOwner(testPlanId);
        return testPlanService.deleteTestPlan(testPlanId);
    }

    @PostMapping("/relevance")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.ASSOCIATE_CASE, content = "#msClass.getLogDetails(#request)", msClass = TestPlanService.class)
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
        api.setOnSampleError(false);
        api.setReportType("iddReport");
        String apiRunConfig = JSONObject.toJSONString(api);
        return testPlanService.run(testplanRunRequest.getTestPlanId(), testplanRunRequest.getProjectId(), testplanRunRequest.getUserId(), testplanRunRequest.getTriggerMode(), apiRunConfig);
    }

    @PostMapping("/copy/{id}")
    @SendNotice(taskType = NoticeConstants.TaskType.TEST_PLAN_TASK, event = NoticeConstants.Event.CREATE, mailTemplate = "track/TestPlanStart", subject = "测试计划通知")
    public TestPlan copy(@PathVariable String id) {
        return testPlanService.copy(id);
    }

    @PostMapping("/api/case/env")
    public Map<String, List<String>> getApiCaseEnv(@RequestBody List<String> caseIds) {
        return testPlanService.getApiCaseEnv(caseIds);
    }

    @PostMapping("/api/scenario/env")
    public Map<String, List<String>> getApiScenarioEnv(@RequestBody List<String> caseIds) {
        return testPlanService.getApiScenarioEnv(caseIds);
    }

    @PostMapping("/case/env")
    public Map<String, List<String>> getPlanCaseEnv(@RequestBody TestPlan plan) {
        return testPlanService.getPlanCaseEnv(plan.getId());
    }

    @PostMapping("/run")
    public String run(@RequestBody TestplanRunRequest testplanRunRequest) {
        return testPlanService.runPlan(testplanRunRequest);
    }

    @GetMapping("/report/export/{planId}")
    public void exportHtmlReport(@PathVariable String planId, HttpServletResponse response) throws UnsupportedEncodingException {
        testPlanService.exportPlanReport(planId, response);
    }

    @GetMapping("/get/report/export/{planId}")
    public TestPlanSimpleReportDTO getExportHtmlReport(@PathVariable String planId, HttpServletResponse response) throws UnsupportedEncodingException {
        return testPlanService.buildPlanReport(planId, true);
    }

    @GetMapping("/report/db/export/{reportId}")
    public void exportHtmlDbReport(@PathVariable String reportId, HttpServletResponse response) throws UnsupportedEncodingException {
        testPlanService.exportPlanDbReport(reportId, response);
    }

    @GetMapping("/report/{planId}")
    public TestPlanSimpleReportDTO getReport(@PathVariable String planId) {
        return testPlanService.getReport(planId, null);
    }

    @GetMapping("/report/functional/result")
    public TestCaseReportStatusResultDTO getFunctionalResultReport(@PathVariable String planId) {
        return testPlanService.getFunctionalResultReport(planId);
    }

    @PostMapping("/edit/report")
    public void editReport(@RequestBody TestPlanWithBLOBs testPlanWithBLOBs) {
        testPlanService.editReport(testPlanWithBLOBs);
    }

    @PostMapping(value = "/schedule/updateEnableByPrimyKey")
    public Schedule updateScheduleEnableByPrimyKey(@RequestBody ScheduleInfoRequest request) {
        Schedule schedule = scheduleService.getSchedule(request.getTaskID());
        schedule.setEnable(request.isEnable());
        apiAutomationService.updateSchedule(schedule);
        return schedule;
    }

    @PostMapping(value = "/schedule/updateEnableByPrimyKey/disable")
    @SendNotice(taskType = NoticeConstants.TaskType.TRACK_HOME_TASK, event = NoticeConstants.Event.CLOSE_SCHEDULE, mailTemplate = "track/ScheduleClose", subject = "测试跟踪通知")
    public Schedule disableSchedule(@RequestBody ScheduleInfoRequest request) {
        Schedule schedule = scheduleService.getSchedule(request.getTaskID());
        schedule.setEnable(false);
        apiAutomationService.updateSchedule(schedule);
        return schedule;
    }

    @GetMapping("/have/exec/case/{id}")
    public boolean haveExecCase(@PathVariable String id) {
        return testPlanService.haveExecCase(id);
    }

    @GetMapping("/principal/{planId}")
    public List<User> getPlanPrincipal(@PathVariable String planId) {
        return testPlanService.getPlanPrincipal(planId);
    }

    @GetMapping("/follow/{planId}")
    public List<User> getPlanFollow(@PathVariable String planId) {
        return testPlanService.getPlanFollow(planId);
    }

    @PostMapping(value = "/schedule/Batch/updateEnable")
    public void updateBatchScheduleEnable(@RequestBody ScheduleInfoRequest request) {
        testPlanService.batchUpdateScheduleEnable(request);
    }

    @PostMapping(value = "/schedule/enable/total")
    public long countByScheduleEnableTotal(@RequestBody QueryTestPlanRequest request) {
        return testPlanService.countScheduleEnableTotal(request);
    }

    @PostMapping(value = "/update/scheduleByEnable")
    public ScheduleDTO updateTestPlanBySchedule(@RequestBody ScheduleInfoRequest request) {
        return testPlanService.updateTestPlanBySchedule(request);
    }
}
