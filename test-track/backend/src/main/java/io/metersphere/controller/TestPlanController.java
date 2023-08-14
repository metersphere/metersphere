package io.metersphere.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.*;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.dto.ScheduleDTO;
import io.metersphere.dto.TestPlanDTOWithMetric;
import io.metersphere.dto.TestPlanRerunParametersDTO;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.log.annotation.MsRequestLog;
import io.metersphere.log.vo.StatusReference;
import io.metersphere.notice.annotation.SendNotice;
import io.metersphere.plan.dto.TestCaseReportStatusResultDTO;
import io.metersphere.plan.dto.TestPlanDTO;
import io.metersphere.plan.dto.TestPlanReportDataStruct;
import io.metersphere.plan.request.AddTestPlanRequest;
import io.metersphere.plan.request.BatchOperateRequest;
import io.metersphere.plan.request.QueryTestPlanRequest;
import io.metersphere.plan.request.ScheduleInfoRequest;
import io.metersphere.plan.request.api.TestPlanRunRequest;
import io.metersphere.plan.request.function.PlanCaseRelevanceRequest;
import io.metersphere.plan.request.function.TestCaseRelevanceRequest;
import io.metersphere.plan.service.TestPlanProjectService;
import io.metersphere.plan.service.TestPlanRerunService;
import io.metersphere.plan.service.TestPlanService;
import io.metersphere.request.ScheduleRequest;
import io.metersphere.service.BaseScheduleService;
import io.metersphere.service.BaseUserService;
import io.metersphere.service.wapper.CheckPermissionService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

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
    private BaseScheduleService baseScheduleService;
    @Resource
    private BaseUserService baseUserService;
    @Resource
    private TestPlanRerunService testPlanRerunService;

    @GetMapping("/auto-check/{testPlanId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public void autoCheck(@PathVariable String testPlanId) {
        testPlanService.checkTestPlanStatus(testPlanId);
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public Pager<List<TestPlanDTOWithMetric>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestPlanRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testPlanService.listTestPlan(request));
    }

    @PostMapping("/metric")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public List<TestPlanDTOWithMetric> selectTestPlanMetricById(@RequestBody List<String> ids) {
        return testPlanService.selectTestPlanMetricById(ids);
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

    @PostMapping("/list/all/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public Pager<List<TestPlanDTO>> planListAll(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestPlanRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testPlanService.planListAll(request));
    }

    @GetMapping("/get/stage/option/{projectId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public List getStageOption(@PathVariable("projectId") String projectId) {
        return testPlanService.getStageOption(projectId);
    }

    @GetMapping("recent/{count}/{id}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public List<TestPlan> recentTestPlans(@PathVariable("count") int count, @PathVariable("id") String projectId) {
        PageHelper.startPage(1, count, true);
        return testPlanService.recentTestPlans(projectId);
    }

    @GetMapping("/get/{testPlanId}")
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
        TestPlan result = testPlanService.addTestPlan(testPlan);
        result.setStage(StatusReference.statusMap.containsKey(result.getStage()) ? StatusReference.statusMap.get(result.getStage()) : result.getStage());
        result.setStatus(StatusReference.statusMap.containsKey(result.getStatus()) ? StatusReference.statusMap.get(result.getStatus()) : result.getStatus());
        return result;
    }

    @PostMapping("/edit")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_EDIT)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#testPlanDTO.id)", content = "#msClass.getLogDetails(#testPlanDTO.id)", msClass = TestPlanService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.TEST_PLAN_TASK, event = NoticeConstants.Event.UPDATE, subject = "测试计划通知")
    public TestPlan editTestPlan(@RequestBody AddTestPlanRequest testPlanDTO) {
        TestPlan testPlan = testPlanService.editTestPlanWithRequest(testPlanDTO);
        testPlan.setStage(StatusReference.statusMap.containsKey(testPlan.getStage()) ? StatusReference.statusMap.get(testPlan.getStage()) : testPlan.getStage());
        testPlan.setStatus(StatusReference.statusMap.containsKey(testPlan.getStatus()) ? StatusReference.statusMap.get(testPlan.getStatus()) : testPlan.getStatus());
        return testPlan;
    }

    @PostMapping("/fresh/{planId}")
    @MsRequestLog(module = OperLogModule.TRACK_TEST_PLAN)
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public TestPlan freshRecentPlan(@PathVariable String planId) {
        AddTestPlanRequest request = new AddTestPlanRequest();
        request.setId(planId);
        request.setUpdateTime(System.currentTimeMillis());
        return testPlanService.editTestPlanWithRequest(request);
    }

    @PostMapping("/edit/status/{planId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_EDIT)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#planId)", content = "#msClass.getLogDetails(#planId)", msClass = TestPlanService.class)
    public void editTestPlanStatus(@PathVariable String planId) {
        checkPermissionService.checkTestPlanOwner(planId);
        testPlanService.checkTestPlanStatus(planId);
    }

    @PostMapping("/edit/report/config")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_EDIT)
    @MsRequestLog(module = OperLogModule.TRACK_TEST_PLAN)
    public void editReportConfig(@RequestBody TestPlanDTO testPlanDTO) {
        testPlanService.editReportConfig(testPlanDTO);
    }

    @PostMapping("/edit/follows/{planId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_EDIT)
    @MsRequestLog(module = OperLogModule.TRACK_TEST_PLAN)
    public void editTestFollows(@PathVariable String planId, @RequestBody List<String> follows) {
        testPlanService.editTestFollows(planId, follows);
    }

    @PostMapping("/delete/{testPlanId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_DELETE)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#testPlanId)", msClass = TestPlanService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.TEST_PLAN_TASK, target = "#targetClass.getTransferPlan(#testPlanId)", targetClass = TestPlanService.class,
            event = NoticeConstants.Event.DELETE, subject = "测试计划通知")
    public int deleteTestPlan(@PathVariable String testPlanId) {
        checkPermissionService.checkTestPlanOwner(testPlanId);
        return testPlanService.deleteTestPlan(testPlanId);
    }

    @PostMapping("/delete/batch")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_BATCH_DELETE)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.BATCH_DEL, project = "#request.projectId", beforeEvent = "#msClass.getDeleteBatchLogDetails(#request)", msClass = TestPlanService.class)
    public void deleteTestPlanBatch(@RequestBody BatchOperateRequest request) {
        testPlanService.deleteTestPlanBatch(request);
    }

    @PostMapping("/relevance")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.ASSOCIATE_CASE, content = "#msClass.getLogDetails(#request)", msClass = TestPlanService.class)
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_RELEVANCE_OR_CANCEL)
    public void testPlanRelevance(@RequestBody PlanCaseRelevanceRequest request) {
        testPlanService.testPlanRelevance(request);
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
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.COPY, content = "#msClass.getLogDetails(#id)", msClass = TestPlanService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.TEST_PLAN_TASK, event = NoticeConstants.Event.CREATE, subject = "测试计划通知")
    public TestPlan copy(@PathVariable String id) {
        TestPlan result = testPlanService.copy(id);
        result.setStage(StatusReference.statusMap.containsKey(result.getStage()) ? StatusReference.statusMap.get(result.getStage()) : result.getStage());
        result.setStatus(StatusReference.statusMap.containsKey(result.getStatus()) ? StatusReference.statusMap.get(result.getStatus()) : result.getStatus());
        return result;
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

    @PostMapping("/case/relevance/project/ids")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public List<String> getRelevanceProjectIds(@RequestBody TestPlan plan) {
        return testPlanService.getRelevanceProjectIds(plan.getId());
    }

    @GetMapping("/case/relevance/project/id/{testPlanId}/{caseType}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public List<String> getRelevanceProjectIds(@PathVariable String testPlanId, @PathVariable String caseType) {
        return testPlanService.getRelevanceProjectIdsByCaseType(testPlanId, caseType);
    }


    @PostMapping("/edit/run/config")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_RUN)
    @MsRequestLog(module = OperLogModule.TRACK_TEST_PLAN)
    public void updateRunModeConfig(@RequestBody TestPlanRunRequest testplanRunRequest) {
        testPlanService.updateRunModeConfig(testplanRunRequest);
    }

    @PostMapping("/run")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_RUN)
    @MsRequestLog(module = OperLogModule.TRACK_TEST_PLAN)
    public String run(@RequestBody TestPlanRunRequest testplanRunRequest) {
        //检查用户是否存在
        baseUserService.checkUserAndProject(testplanRunRequest.getUserId(), testplanRunRequest.getProjectId());
        return testPlanService.runPlan(testplanRunRequest);
    }

    @PostMapping("/run/save")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_RUN)
    @MsRequestLog(module = OperLogModule.TRACK_TEST_PLAN)
    public String runAndSave(@RequestBody TestPlanRunRequest testplanRunRequest) {
        //检查用户是否存在
        baseUserService.checkUserAndProject(testplanRunRequest.getUserId(), testplanRunRequest.getProjectId());
        testPlanService.updateRunModeConfig(testplanRunRequest);
        return testPlanService.runPlan(testplanRunRequest);
    }

    @PostMapping(value = "/run/batch")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_RUN)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.EXECUTE, content = "#msClass.getLogDetails(#request.testPlanIds)", msClass = TestPlanService.class)
    public void runBatch(@RequestBody TestPlanRunRequest request) {
        baseUserService.checkUserAndProject(request.getUserId(), request.getProjectId());
        request.setTriggerMode(TriggerMode.BATCH.name());
        testPlanService.runBatch(request);
    }

    @GetMapping("/report/export/{planId}/{lang}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REPORT_READ_EXPORT)
    public void exportHtmlReport(@PathVariable String planId, @PathVariable(required = false) String lang, HttpServletResponse response) throws UnsupportedEncodingException, JsonProcessingException {
        testPlanService.exportPlanReport(planId, lang, response);
    }

    @GetMapping("/get/report/export/{planId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REPORT_READ_EXPORT)
    public TestPlanReportDataStruct getExportHtmlReport(@PathVariable String planId, HttpServletResponse response) throws UnsupportedEncodingException {
        return testPlanService.buildPlanReport(planId, true);
    }

    @GetMapping("/report/db/export/{reportId}/{lang}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REPORT_READ_EXPORT)
    public void exportHtmlDbReport(@PathVariable String reportId, @PathVariable(required = false) String lang, HttpServletResponse response) throws UnsupportedEncodingException, JsonProcessingException {
        testPlanService.exportPlanDbReport(reportId, lang, response);
    }

    @GetMapping("/report/functional/result")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REPORT_READ)
    public TestCaseReportStatusResultDTO getFunctionalResultReport(@PathVariable String planId) {
        return testPlanService.getFunctionalResultReport(planId);
    }

    @PostMapping("/edit/report")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REPORT_READ_EDIT)
    @MsRequestLog(module = OperLogModule.TRACK_TEST_PLAN)
    public void editReport(@RequestBody TestPlanWithBLOBs testPlanWithBLOBs) {
        testPlanService.editReport(testPlanWithBLOBs);
    }

    @PostMapping(value = "/schedule/update/enable")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_SCHEDULE)
    public Schedule updateScheduleEnableByPrimaryKey(@RequestBody ScheduleInfoRequest request) {
        Schedule schedule = baseScheduleService.getSchedule(request.getTaskID());
        schedule.setEnable(request.isEnable());
        testPlanService.updateSchedule(schedule);
        return schedule;
    }

    @PostMapping(value = "/schedule/update/disable")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_SCHEDULE)
    @SendNotice(taskType = NoticeConstants.TaskType.TRACK_HOME_TASK, event = NoticeConstants.Event.CLOSE_SCHEDULE, subject = "测试跟踪通知")
    public Schedule disableSchedule(@RequestBody ScheduleInfoRequest request) {
        Schedule schedule = baseScheduleService.getSchedule(request.getTaskID());
        schedule.setEnable(false);
        testPlanService.updateSchedule(schedule);
        return schedule;
    }

    /***
     * 只检查是否有API，性能用例
     * @param id
     * @return
     */
    @GetMapping("/have/exec/case/{id}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public boolean haveExecCase(@PathVariable String id) {
        return testPlanService.haveExecCase(id, true);
    }

    /**
     * 该测试计划是否包含ui场景
     *
     * @param id
     * @return
     */
    @GetMapping("/have/ui/case/{id}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public boolean haveUiCase(@PathVariable String id) {
        return testPlanService.haveUiCase(id);
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

    @PostMapping(value = "/schedule/batch/update_enable")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_SCHEDULE)
    @MsRequestLog(module = OperLogModule.TRACK_TEST_PLAN_SCHEDULE)
    public void updateBatchScheduleEnable(@RequestBody ScheduleInfoRequest request) {
        testPlanService.batchUpdateScheduleEnable(request);
    }

    @PostMapping(value = "/schedule/enable/total")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public long countByScheduleEnableTotal(@RequestBody QueryTestPlanRequest request) {
        return testPlanService.countScheduleEnableTotal(request);
    }

    @PostMapping(value = "/update/schedule/enable")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_SCHEDULE)
    public ScheduleDTO updateTestPlanBySchedule(@RequestBody ScheduleInfoRequest request) {
        Schedule schedule = baseScheduleService.getSchedule(request.getTaskID());
        schedule.setEnable(request.isEnable());
        testPlanService.updateSchedule(schedule);
        return testPlanService.getNextTriggerSchedule(schedule);
    }

    @PostMapping(value = "/schedule/update")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_SCHEDULE)
    public void updateSchedule(@RequestBody Schedule request) {
        testPlanService.updateSchedule(request);
    }

    @PostMapping(value = "/schedule/create")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_SCHEDULE)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN_SCHEDULE, type = OperLogConstants.CREATE,
            title = "#request.name", content = "#msClass.getLogDetails(#request)", msClass = BaseScheduleService.class)
    public void createSchedule(@RequestBody ScheduleRequest request) {
        testPlanService.createSchedule(request);
    }


    @GetMapping("/schedule/get/{testId}/{group}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public Schedule schedule(@PathVariable String testId, @PathVariable String group) {
        Schedule schedule = baseScheduleService.getScheduleByResource(testId, group);
        return schedule;
    }

    @PostMapping(value = "/rerun")
    @MsRequestLog(module = OperLogModule.TRACK_TEST_PLAN)
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_RUN)
    public String rerun(@RequestBody TestPlanRerunParametersDTO request) {
        return testPlanRerunService.rerun(request);
    }

    @GetMapping(value = "/status/reset/{planId}")
    @MsRequestLog(module = OperLogModule.TRACK_TEST_PLAN)
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public void resetStatus(@PathVariable String planId) {
        testPlanService.resetStatus(planId);
    }
}
