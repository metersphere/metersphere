package io.metersphere.api.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.*;
import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.dto.datacount.ExecutedCaseInfoResult;
import io.metersphere.api.dto.datacount.request.ScheduleInfoRequest;
import io.metersphere.api.dto.datacount.response.ApiDataCountDTO;
import io.metersphere.api.dto.datacount.response.ExecutedCaseInfoDTO;
import io.metersphere.api.dto.datacount.response.TaskInfoResult;
import io.metersphere.api.dto.definition.RunDefinitionRequest;
import io.metersphere.api.dto.scenario.request.dubbo.RegistryCenter;
import io.metersphere.api.service.*;
import io.metersphere.base.domain.ApiTest;
import io.metersphere.base.domain.Schedule;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.CronUtils;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.QueryScheduleRequest;
import io.metersphere.dto.ScheduleDao;
import io.metersphere.performance.service.PerformanceTestService;
import io.metersphere.service.CheckPermissionService;
import io.metersphere.service.ScheduleService;
import org.apache.jorphan.collections.HashTree;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static io.metersphere.commons.utils.JsonPathUtils.getListJson;


@RestController
@RequestMapping(value = "/api")
@RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
public class APITestController {
    @Resource
    private APITestService apiTestService;
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private CheckPermissionService checkownerService;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private ApiDefinitionExecResultService apiDefinitionExecResultService;
    @Resource
    private ApiAutomationService apiAutomationService;
    @Resource
    private ApiScenarioReportService apiScenarioReportService;
    @Resource
    private ScheduleService scheduleService;
    @Resource
    private APIReportService apiReportService;
    @Resource
    private PerformanceTestService performanceTestService;
    @Resource
    private CheckPermissionService checkPermissionService;
    @Resource
    private HistoricalDataUpgradeService historicalDataUpgradeService;

    @GetMapping("recent/{count}")
    public List<APITestResult> recentTest(@PathVariable int count) {
        String currentWorkspaceId = SessionUtils.getCurrentWorkspaceId();
        QueryAPITestRequest request = new QueryAPITestRequest();
        request.setWorkspaceId(currentWorkspaceId);
        request.setUserId(SessionUtils.getUserId());
        PageHelper.startPage(1, count, true);
        return apiTestService.recentTest(request);
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<APITestResult>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryAPITestRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        request.setProjectId(SessionUtils.getCurrentProjectId());
        return PageUtils.setPageInfo(page, apiTestService.list(request));
    }

    @PostMapping("/list/ids")
    public List<ApiTest> listByIds(@RequestBody QueryAPITestRequest request) {
        return apiTestService.listByIds(request);
    }

    @GetMapping("/list/{projectId}")
    public List<ApiTest> list(@PathVariable String projectId) {
        checkownerService.checkProjectOwner(projectId);
        return apiTestService.getApiTestByProjectId(projectId);
    }

    @PostMapping(value = "/schedule/update")
    public void updateSchedule(@RequestBody Schedule request) {
        apiTestService.updateSchedule(request);
    }

    @PostMapping(value = "/schedule/create")
    public void createSchedule(@RequestBody Schedule request) {
        apiTestService.createSchedule(request);
    }

    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public void create(@RequestPart("request") SaveAPITestRequest request, @RequestPart(value = "file") MultipartFile file, @RequestPart(value = "files") List<MultipartFile> bodyFiles) {
        apiTestService.create(request, file, bodyFiles);
    }

    @PostMapping(value = "/create/merge", consumes = {"multipart/form-data"})
    public void mergeCreate(@RequestPart("request") SaveAPITestRequest request, @RequestPart(value = "file") MultipartFile file, @RequestPart(value = "selectIds") List<String> selectIds) {
        apiTestService.mergeCreate(request, file, selectIds);
    }

    @PostMapping(value = "/update", consumes = {"multipart/form-data"})
    public void update(@RequestPart("request") SaveAPITestRequest request, @RequestPart(value = "file") MultipartFile file, @RequestPart(value = "files") List<MultipartFile> bodyFiles) {
        checkownerService.checkApiTestOwner(request.getId());
        apiTestService.update(request, file, bodyFiles);
    }

    @PostMapping(value = "/copy")
    public void copy(@RequestBody SaveAPITestRequest request) {
        apiTestService.copy(request);
    }

    @GetMapping("/get/{testId}")
    public APITestResult get(@PathVariable String testId) {
        checkownerService.checkApiTestOwner(testId);
        return apiTestService.get(testId);
    }


    @PostMapping("/delete")
    public void delete(@RequestBody DeleteAPITestRequest request) {
        checkownerService.checkApiTestOwner(request.getId());
        apiTestService.delete(request);
    }

    @PostMapping(value = "/run")
    public String run(@RequestBody SaveAPITestRequest request) {
        return apiTestService.run(request);
    }

    @PostMapping(value = "/run/debug", consumes = {"multipart/form-data"})
    public String runDebug(@RequestPart("request") SaveAPITestRequest request, @RequestPart(value = "file") MultipartFile file, @RequestPart(value = "files") List<MultipartFile> bodyFiles) {
        return apiTestService.runDebug(request, file, bodyFiles);
    }

    @PostMapping(value = "/checkName")
    public void checkName(@RequestBody SaveAPITestRequest request) {
        apiTestService.checkName(request);
    }

    @PostMapping(value = "/import", consumes = {"multipart/form-data"})
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public ApiTest testCaseImport(@RequestPart(value = "file", required = false) MultipartFile file, @RequestPart("request") ApiTestImportRequest request) {
        return apiTestService.apiTestImport(file, request);
    }

    @PostMapping("/dubbo/providers")
    public List<DubboProvider> getProviders(@RequestBody RegistryCenter registry) {
        return apiTestService.getProviders(registry);
    }

    @PostMapping("/list/schedule/{goPage}/{pageSize}")
    public List<ScheduleDao> listSchedule(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryScheduleRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return apiTestService.listSchedule(request);
    }

    @PostMapping("/list/schedule")
    public List<ScheduleDao> listSchedule(@RequestBody QueryScheduleRequest request) {
        return apiTestService.listSchedule(request);
    }

    @PostMapping("/getJsonPaths")
    public List<HashMap> getJsonPaths(@RequestBody QueryJsonPathRequest request) {
        return getListJson(request.getJsonPath());
    }

    @GetMapping("/apiCount/{projectId}")
    public ApiDataCountDTO apiCount(@PathVariable String projectId) {

        ApiDataCountDTO apiCountResult = new ApiDataCountDTO();

        List<ApiDataCountResult> countResultByProtocolList = apiDefinitionService.countProtocolByProjectID(projectId);
        apiCountResult.countProtocal(countResultByProtocolList);

        long dateCountByCreateInThisWeek = apiDefinitionService.countByProjectIDAndCreateInThisWeek(projectId);
        apiCountResult.setThisWeekAddedCount(dateCountByCreateInThisWeek);

        //查询完成率、进行中、已完成
        List<ApiDataCountResult> countResultByStatelList = apiDefinitionService.countStateByProjectID(projectId);
        apiCountResult.countStatus(countResultByStatelList);
        long allCount = apiCountResult.getFinishedCount() + apiCountResult.getRunningCount() + apiCountResult.getNotStartedCount();

        if (allCount != 0) {
            float complateRageNumber = (float) apiCountResult.getFinishedCount() * 100 / allCount;
            DecimalFormat df = new DecimalFormat("0.0");
            apiCountResult.setCompletionRage(df.format(complateRageNumber) + "%");
        }

        apiCountResult.setHttpCountStr("HTTP&nbsp;&nbsp;<br/><br/>" + apiCountResult.getHttpApiDataCountNumber());
        apiCountResult.setRpcCountStr("RPC&nbsp;&nbsp;<br/><br/>" + apiCountResult.getRpcApiDataCountNumber());
        apiCountResult.setTcpCountStr("TCP&nbsp;&nbsp;<br/><br/>" + apiCountResult.getTcpApiDataCountNumber());
        apiCountResult.setSqlCountStr("SQL&nbsp;&nbsp;<br/><br/>" + apiCountResult.getSqlApiDataCountNumber());
        return apiCountResult;
    }

    @GetMapping("/testCaseInfoCount/{projectId}")
    public ApiDataCountDTO testCaseInfoCount(@PathVariable String projectId) {
        ApiDataCountDTO apiCountResult = new ApiDataCountDTO();

        List<ApiDataCountResult> countResultList = apiTestCaseService.countProtocolByProjectID(projectId);
        apiCountResult.countProtocal(countResultList);

        long dateCountByCreateInThisWeek = apiTestCaseService.countByProjectIDAndCreateInThisWeek(projectId);
        apiCountResult.setThisWeekAddedCount(dateCountByCreateInThisWeek);

        long executedInThisWeekCountNumber = apiDefinitionExecResultService.countByTestCaseIDInProjectAndExecutedInThisWeek(projectId);
        apiCountResult.setThisWeekExecutedCount(executedInThisWeekCountNumber);
        long executedCountNumber = apiDefinitionExecResultService.countByTestCaseIDInProject(projectId);
        apiCountResult.setExecutedCount(executedCountNumber);

        //未覆盖 已覆盖： 统计当前接口下是否含有案例
        List<ApiDataCountResult> countResultByApiCoverageList = apiDefinitionService.countApiCoverageByProjectID(projectId);
        apiCountResult.countApiCoverage(countResultByApiCoverageList);
        long allCount = apiCountResult.getCoverageCount() + apiCountResult.getUncoverageCount();

        if (allCount != 0) {
            float coverageRageNumber = (float) apiCountResult.getCoverageCount() * 100 / allCount;
            DecimalFormat df = new DecimalFormat("0.0");
            apiCountResult.setCoverageRage(df.format(coverageRageNumber) + "%");
        }


        apiCountResult.setHttpCountStr("HTTP&nbsp;&nbsp;<br/><br/>" + apiCountResult.getHttpApiDataCountNumber());
        apiCountResult.setRpcCountStr("RPC&nbsp;&nbsp;<br/><br/>" + apiCountResult.getRpcApiDataCountNumber());
        apiCountResult.setTcpCountStr("TCP&nbsp;&nbsp;<br/><br/>" + apiCountResult.getTcpApiDataCountNumber());
        apiCountResult.setSqlCountStr("SQL&nbsp;&nbsp;<br/><br/>" + apiCountResult.getSqlApiDataCountNumber());

        return apiCountResult;
    }

    @GetMapping("/testSceneInfoCount/{projectId}")
    public ApiDataCountDTO testSceneInfoCount(@PathVariable String projectId) {

        ApiDataCountDTO apiCountResult = new ApiDataCountDTO();

        long scenarioCountNumber = apiAutomationService.countScenarioByProjectID(projectId);
        apiCountResult.setAllApiDataCountNumber(scenarioCountNumber);

        /**
         *  本周新增：通过测试场景的createTime
         *  本周执行: 查询（本周）生成的测试报告
         *  历史总执行：查询所有的测试报告
         * */
        long dateCountByCreateInThisWeek = apiAutomationService.countScenarioByProjectIDAndCreatInThisWeek(projectId);
        apiCountResult.setThisWeekAddedCount(dateCountByCreateInThisWeek);
        long executedInThisWeekCountNumber = apiScenarioReportService.countByProjectIdAndCreateInThisWeek(projectId);
        apiCountResult.setThisWeekExecutedCount(executedInThisWeekCountNumber);
        long executedCountNumber = apiScenarioReportService.countByProjectID(projectId);
        apiCountResult.setExecutedCount(executedCountNumber);

        //未执行、未通过、已通过
        List<ApiDataCountResult> countResultByRunResult = apiAutomationService.countRunResultByProjectID(projectId);
        apiCountResult.countRunResult(countResultByRunResult);

        long allCount = apiCountResult.getUnexecuteCount() + apiCountResult.getExecutionPassCount() + apiCountResult.getExecutionFailedCount();

        if (allCount != 0) {
            float coverageRageNumber = (float) apiCountResult.getExecutionPassCount() * 100 / allCount;
            DecimalFormat df = new DecimalFormat("0.0");
            apiCountResult.setPassRage(df.format(coverageRageNumber) + "%");
        }

        return apiCountResult;

    }

    @GetMapping("/scheduleTaskInfoCount/{projectId}")
    public ApiDataCountDTO scheduleTaskInfoCount(@PathVariable String projectId) {
        ApiDataCountDTO apiCountResult = new ApiDataCountDTO();

        long allTaskCount = scheduleService.countTaskByProjectId(projectId);

        apiCountResult.setAllApiDataCountNumber(allTaskCount);

        long taskCountInThisWeek = scheduleService.countTaskByProjectIdInThisWeek(projectId);
        apiCountResult.setThisWeekAddedCount(taskCountInThisWeek);
//        long api_executedInThisWeekCountNumber = apiReportService.countByProjectIdAndCreateInThisWeek(projectId);
        long executedInThisWeekCountNumber = apiScenarioReportService.countByProjectIdAndCreateAndByScheduleInThisWeek(projectId);
//        long executedInThisWeekCountNumber = api_executedInThisWeekCountNumber+scene_executedInThisWeekCountNumber;
        apiCountResult.setThisWeekExecutedCount(executedInThisWeekCountNumber);

        //统计 失败 成功 以及总数
        List<ApiDataCountResult> allExecuteResult = apiScenarioReportService.countByProjectIdGroupByExecuteResult(projectId);
        apiCountResult.countScheduleExecute(allExecuteResult);

        long allCount = apiCountResult.getExecutedCount();
        if (allCount != 0) {
            float coverageRageNumber = (float) apiCountResult.getSuccessCount() * 100 / allCount;
            DecimalFormat df = new DecimalFormat("0.0");
            apiCountResult.setSuccessRage(df.format(coverageRageNumber) + "%");
        }

        return apiCountResult;
    }

    @GetMapping("/faliureCaseAboutTestPlan/{projectId}/{limitNumber}")
    public List<ExecutedCaseInfoDTO> faliureCaseAboutTestPlan(@PathVariable String projectId, @PathVariable int limitNumber) {

        List<ExecutedCaseInfoResult> selectDataList = apiDefinitionExecResultService.findFaliureCaseInfoByProjectIDAndLimitNumberInSevenDays(projectId, limitNumber);

        List<ExecutedCaseInfoDTO> returnList = new ArrayList<>(limitNumber);

        for (int dataIndex = 0; dataIndex < limitNumber; dataIndex++) {

            ExecutedCaseInfoDTO dataDTO = new ExecutedCaseInfoDTO();
            dataDTO.setSortIndex(dataIndex + 1);

            if (dataIndex < selectDataList.size()) {
                ExecutedCaseInfoResult selectData = selectDataList.get(dataIndex);
                dataDTO.setCaseID(selectData.getTestCaseID());
                dataDTO.setCaseName(selectData.getCaseName());
                dataDTO.setTestPlan(selectData.getTestPlan());
                dataDTO.setFailureTimes(selectData.getFailureTimes());
                dataDTO.setCaseType(selectData.getCaseType());
                dataDTO.setTestPlanDTOList(selectData.getTestPlanDTOList());
            } else {
                dataDTO.setCaseName("");
                dataDTO.setTestPlan("");
            }
            returnList.add(dataDTO);
        }
        return returnList;
    }

    @GetMapping("/runningTask/{projectID}")
    public List<TaskInfoResult> runningTask(@PathVariable String projectID) {

        List<TaskInfoResult> resultList = scheduleService.findRunningTaskInfoByProjectID(projectID);
        int dataIndex = 1;
        for (TaskInfoResult taskInfo :
                resultList) {
            taskInfo.setIndex(dataIndex++);
            Date nextExecutionTime = CronUtils.getNextTriggerTime(taskInfo.getRule());
            if (nextExecutionTime != null) {
                taskInfo.setNextExecutionTime(nextExecutionTime.getTime());
            }
        }
        return resultList;
    }

    @PostMapping(value = "/schedule/updateEnableByPrimyKey")
    public void updateScheduleEnableByPrimyKey(@RequestBody ScheduleInfoRequest request) {
        Schedule schedule = scheduleService.getSchedule(request.getTaskID());
        schedule.setEnable(request.isEnable());
        apiAutomationService.updateSchedule(schedule);
    }

    @PostMapping(value = "/historicalDataUpgrade")
    public String historicalDataUpgrade(@RequestBody SaveHistoricalDataUpgrade request) {
        return historicalDataUpgradeService.upgrade(request);
    }

    @PostMapping(value = "/genPerformanceTestXml", consumes = {"multipart/form-data"})
    public JmxInfoDTO genPerformanceTest(@RequestPart("request") RunDefinitionRequest runRequest, @RequestPart(value = "files") List<MultipartFile> bodyFiles) throws Exception {
        HashTree hashTree = runRequest.getTestElement().generateHashTree();
        String jmxString = runRequest.getTestElement().getJmx(hashTree);

        String testName = runRequest.getName();

        //将jmx处理封装为通用方法
        jmxString = apiTestService.updateJmxString(jmxString,testName,false);

        JmxInfoDTO dto = new JmxInfoDTO();
        dto.setName(runRequest.getName() + ".jmx");
        dto.setXml(jmxString);
        return dto;
    }

}
