package io.metersphere.track.controller;

import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.datacount.request.ScheduleInfoRequest;
import io.metersphere.api.service.ApiAutomationService;
import io.metersphere.base.domain.*;
import io.metersphere.commons.constants.*;
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
import io.metersphere.track.request.testplan.TestPlanRunRequest;
import io.metersphere.track.request.testplancase.TestCaseRelevanceRequest;
import io.metersphere.track.service.TestPlanProjectService;
import io.metersphere.track.service.TestPlanService;
import org.apache.shiro.authz.annotation.Logical;
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
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public void autoCheck(@PathVariable String testPlanId) {
        testPlanService.checkStatus(testPlanId);
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public Pager<List<TestPlanDTOWithMetric>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestPlanRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testPlanService.listTestPlan(request));
    }

    @PostMapping("/dashboard/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public Pager<List<TestPlanDTOWithMetric>> listByWorkspaceId(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestPlanRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testPlanService.listByWorkspaceId(request));
    }

    /*jenkins测试计划*/
    @GetMapping("/list/all/{projectId}/{workspaceId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
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
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public JSONArray getStageOption(@PathVariable("projectId") String projectId) {
        return testPlanService.getStageOption(projectId);
    }

    @GetMapping("recent/{count}/{id}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
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
    @SendNotice(taskType = NoticeConstants.TaskType.TEST_PLAN_TASK, event = NoticeConstants.Event.CREATE, subject = "测试计划通知")
    public TestPlan addTestPlan(@RequestBody AddTestPlanRequest testPlan) {
        testPlan.setId(UUID.randomUUID().toString());
        return testPlanService.addTestPlan(testPlan);
    }

    @PostMapping("/edit")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_EDIT)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#testPlanDTO.id)", content = "#msClass.getLogDetails(#testPlanDTO.id)", msClass = TestPlanService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.TEST_PLAN_TASK, event = NoticeConstants.Event.UPDATE, subject = "测试计划通知")
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
            event = NoticeConstants.Event.DELETE, subject = "测试计划通知")
    public int deleteTestPlan(@PathVariable String testPlanId) {
        checkPermissionService.checkTestPlanOwner(testPlanId);
        return testPlanService.deleteTestPlan(testPlanId);
    }

    @PostMapping("/relevance")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_RELEVANCE_OR_CANCEL)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.ASSOCIATE_CASE, content = "#msClass.getLogDetails(#request)", msClass = TestPlanService.class)
    public void testPlanRelevance(@RequestBody PlanCaseRelevanceRequest request) {
        testPlanService.testPlanRelevance(request);
    }

    @GetMapping("/get/metric/{planId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public TestCaseReportMetricDTO getMetric(@PathVariable String planId) {
        return testPlanService.getMetric(planId);
    }

    @GetMapping("/get/statistics/metric/{planId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public TestCaseReportMetricDTO getStatisticsMetric(@PathVariable String planId) {
        return testPlanService.getStatisticsMetric(planId);
    }

    @GetMapping("/project/name/{planId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public String getProjectNameByPlanId(@PathVariable String planId) {
        return testPlanService.getProjectNameByPlanId(planId);
    }

    @PostMapping("/project")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public List<Project> getProjectByPlanId(@RequestBody TestCaseRelevanceRequest request) {
        List<String> projectIds = testPlanProjectService.getProjectIdsByPlanId(request.getPlanId());
        request.setProjectIds(projectIds);
        return testPlanProjectService.getProjectByPlanId(request);
    }

    @PostMapping("/project/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public Pager<List<Project>> getProjectByPlanId(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody TestCaseRelevanceRequest request) {
        List<String> projectIds = testPlanProjectService.getProjectIdsByPlanId(request.getPlanId());
        request.setProjectIds(projectIds);
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testPlanProjectService.getProjectByPlanId(request));
    }

    @PostMapping("/copy/{id}")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_PLAN_READ_CREATE, PermissionConstants.PROJECT_TRACK_PLAN_READ_COPY}, logical = Logical.OR)
    @SendNotice(taskType = NoticeConstants.TaskType.TEST_PLAN_TASK, event = NoticeConstants.Event.CREATE, subject = "测试计划通知")
    public TestPlan copy(@PathVariable String id) {
        return testPlanService.copy(id);
    }

    @PostMapping("/api/case/env")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public Map<String, List<String>> getApiCaseEnv(@RequestBody List<String> caseIds) {
        return testPlanService.getApiCaseEnv(caseIds);
    }

    @PostMapping("/api/scenario/env")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public Map<String, List<String>> getApiScenarioEnv(@RequestBody List<String> caseIds) {
        return testPlanService.getApiScenarioEnv(caseIds);
    }

    @PostMapping("/case/env")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public Map<String, List<String>> getPlanCaseEnv(@RequestBody TestPlan plan) {
        return testPlanService.getPlanCaseEnv(plan.getId());
    }


    @PostMapping("/edit/runModeConfig")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_RUN)
    public void updateRunModeConfig(@RequestBody TestPlanRunRequest testplanRunRequest) {
        testPlanService.updateRunModeConfig(testplanRunRequest);
    }

    @PostMapping("/run")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_RUN)
    public String run(@RequestBody TestPlanRunRequest testplanRunRequest) {
        return testPlanService.runPlan(testplanRunRequest);
    }

    @PostMapping("/run/save")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_RUN)
    public String runAndSave(@RequestBody TestPlanRunRequest testplanRunRequest) {
        testPlanService.updateRunModeConfig(testplanRunRequest);
        return testPlanService.runPlan(testplanRunRequest);
    }

    @PostMapping(value = "/run/batch")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_RUN)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.EXECUTE, content = "#msClass.getLogDetails(#request.testPlanIds)", msClass = TestPlanService.class)
    public void runBatch(@RequestBody TestPlanRunRequest request) {
        request.setTriggerMode(TriggerMode.BATCH.name());
        testPlanService.runBatch(request);
    }

    @GetMapping("/report/export/{planId}/{lang}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REPORT_READ_EXPORT)
    public void exportHtmlReport(@PathVariable String planId, @PathVariable(required = false) String lang, HttpServletResponse response) throws UnsupportedEncodingException {
        testPlanService.exportPlanReport(planId, lang, response);
    }

    @GetMapping("/get/report/export/{planId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REPORT_READ_EXPORT)
    public TestPlanSimpleReportDTO getExportHtmlReport(@PathVariable String planId, HttpServletResponse response) throws UnsupportedEncodingException {
        return testPlanService.buildPlanReport(planId, true);
    }

    @GetMapping("/report/db/export/{reportId}/{lang}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REPORT_READ_EXPORT)
    public void exportHtmlDbReport(@PathVariable String reportId, @PathVariable(required = false) String lang, HttpServletResponse response) throws UnsupportedEncodingException {
        testPlanService.exportPlanDbReport(reportId, lang, response);
    }

    @GetMapping("/report/{planId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REPORT_READ)
    public TestPlanSimpleReportDTO getReport(@PathVariable String planId) {
        return testPlanService.getReport(planId, null);
    }

    @GetMapping("/report/functional/result")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REPORT_READ)
    public TestCaseReportStatusResultDTO getFunctionalResultReport(@PathVariable String planId) {
        return testPlanService.getFunctionalResultReport(planId);
    }

    @PostMapping("/edit/report")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REPORT_READ)
    public void editReport(@RequestBody TestPlanWithBLOBs testPlanWithBLOBs) {
        testPlanService.editReport(testPlanWithBLOBs);
    }

    @PostMapping(value = "/schedule/updateEnableByPrimyKey")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_SCHEDULE)
    public Schedule updateScheduleEnableByPrimyKey(@RequestBody ScheduleInfoRequest request) {
        Schedule schedule = scheduleService.getSchedule(request.getTaskID());
        schedule.setEnable(request.isEnable());
        apiAutomationService.updateSchedule(schedule);
        return schedule;
    }

    @PostMapping(value = "/schedule/updateEnableByPrimyKey/disable")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_SCHEDULE)
    @SendNotice(taskType = NoticeConstants.TaskType.TRACK_HOME_TASK, event = NoticeConstants.Event.CLOSE_SCHEDULE, subject = "测试跟踪通知")
    public Schedule disableSchedule(@RequestBody ScheduleInfoRequest request) {
        Schedule schedule = scheduleService.getSchedule(request.getTaskID());
        schedule.setEnable(false);
        apiAutomationService.updateSchedule(schedule);
        return schedule;
    }

    @GetMapping("/have/exec/case/{id}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public boolean haveExecCase(@PathVariable String id) {
        return testPlanService.haveExecCase(id);
    }

    @GetMapping("/principal/{planId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public List<User> getPlanPrincipal(@PathVariable String planId) {
        return testPlanService.getPlanPrincipal(planId);
    }

    @GetMapping("/follow/{planId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public List<User> getPlanFollow(@PathVariable String planId) {
        return testPlanService.getPlanFollow(planId);
    }

    @PostMapping(value = "/schedule/Batch/updateEnable")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_SCHEDULE)
    public void updateBatchScheduleEnable(@RequestBody ScheduleInfoRequest request) {
        testPlanService.batchUpdateScheduleEnable(request);
    }

    @PostMapping(value = "/schedule/enable/total")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public long countByScheduleEnableTotal(@RequestBody QueryTestPlanRequest request) {
        return testPlanService.countScheduleEnableTotal(request);
    }

    @PostMapping(value = "/update/scheduleByEnable")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_SCHEDULE)
    public ScheduleDTO updateTestPlanBySchedule(@RequestBody ScheduleInfoRequest request) {
        return testPlanService.updateTestPlanBySchedule(request);
    }

}
