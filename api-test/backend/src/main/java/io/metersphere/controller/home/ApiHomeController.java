package io.metersphere.controller.home;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.DubboProvider;
import io.metersphere.api.dto.JmxInfoDTO;
import io.metersphere.api.dto.RegistryCenter;
import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.dto.datacount.response.ApiDataCountDTO;
import io.metersphere.api.dto.datacount.response.CoveredDTO;
import io.metersphere.api.dto.datacount.response.ExecuteResultCountDTO;
import io.metersphere.api.dto.definition.RunDefinitionRequest;
import io.metersphere.api.dto.export.ScenarioToPerformanceInfoDTO;
import io.metersphere.base.domain.ApiDefinition;
import io.metersphere.base.domain.Schedule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.enums.ExecutionExecuteTypeEnum;
import io.metersphere.commons.utils.*;
import io.metersphere.dto.TaskInfoResult;
import io.metersphere.service.BaseScheduleService;
import io.metersphere.service.definition.ApiDefinitionExecResultService;
import io.metersphere.service.definition.ApiDefinitionService;
import io.metersphere.service.definition.ApiTestCaseService;
import io.metersphere.service.scenario.ApiScenarioReportService;
import io.metersphere.service.scenario.ApiScenarioService;
import io.metersphere.task.service.TaskService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@RestController
@RequestMapping(value = "/home")
public class ApiHomeController {
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private ApiDefinitionExecResultService apiDefinitionExecResultService;
    @Resource
    private ApiScenarioService apiAutomationService;
    @Resource
    private ApiScenarioReportService apiScenarioReportService;
    @Resource
    private BaseScheduleService baseScheduleService;
    @Resource
    private TaskService taskService;


    @GetMapping("/api/count/{projectId}/{versionId}")
    public ApiDataCountDTO apiCount(@PathVariable String projectId, @PathVariable String versionId) {
        versionId = this.initializationVersionId(versionId);
        ApiDataCountDTO apiCountResult = new ApiDataCountDTO();
        //所有有效接口，用于计算不同请求的接口数量、本周创建、覆盖率
        Map<String, List<ApiDefinition>> protocolAllDefinitionMap = apiDefinitionService.countEffectiveByProjectId(projectId, versionId);
        apiCountResult.countProtocol(protocolAllDefinitionMap);
        //本周创建
        apiCountResult.setCreatedInWeek(apiDefinitionService.getApiByCreateInThisWeek(protocolAllDefinitionMap).size());

        //查询完成率、进行中、已完成
        List<ApiDataCountResult> countResultByStateList = apiDefinitionService.countStateByProjectID(projectId, versionId);
        apiCountResult.countStatus(countResultByStateList);
        long allCount = apiCountResult.getFinishedCount() + apiCountResult.getRunningCount() + apiCountResult.getNotStartedCount();
        if (allCount != 0) {
            float completeRateNumber = (float) apiCountResult.getFinishedCount() * 100 / allCount;
            DecimalFormat df = new DecimalFormat("0.0");
            apiCountResult.setCompletedRate(df.format(completeRateNumber) + "%");
        }

        if (apiCountResult.getTotal() == 0) {
            //没有任何接口数据
            apiCountResult.setCoveredCount(0);
            apiCountResult.setNotCoveredCount(0);
        }
        return apiCountResult;
    }

    @GetMapping("/api/case/count/{projectId}/{versionId}")
    public ApiDataCountDTO apiCaseCount(@PathVariable String projectId, @PathVariable String versionId) {
        versionId = this.initializationVersionId(versionId);
        ApiDataCountDTO apiCountResult = new ApiDataCountDTO();
        List<ApiDataCountResult> countResultList = apiTestCaseService.countProtocolByProjectID(projectId, versionId);
        apiCountResult.countProtocol(countResultList);
        //本周创建、本周执行、总执行
        long dateCountByCreateInThisWeek = apiTestCaseService.countByProjectIDAndCreateInThisWeek(projectId, versionId);
        apiCountResult.setCreatedInWeek(dateCountByCreateInThisWeek);
        long executedInThisWeekCountNumber = apiDefinitionExecResultService.countByTestCaseIDInProjectAndExecutedInThisWeek(projectId, versionId);
        apiCountResult.setExecutedTimesInWeek(executedInThisWeekCountNumber);
        long executedCount = apiTestCaseService.countExecutedTimesByProjectId(projectId, ExecutionExecuteTypeEnum.BASIC.name(), versionId);
        apiCountResult.setExecutedTimes(executedCount);
        return apiCountResult;
    }

    @GetMapping("/scenario/count/{projectId}/{versionId}")
    public ApiDataCountDTO scenarioCount(@PathVariable String projectId, @PathVariable String versionId) {
        versionId = this.initializationVersionId(versionId);
        ApiDataCountDTO apiCountResult = new ApiDataCountDTO();
        long scenarioCountNumber = apiAutomationService.countScenarioByProjectID(projectId, versionId);
        apiCountResult.setTotal(scenarioCountNumber);
        //统计覆盖率
        long dateCountByCreateInThisWeek = apiAutomationService.countScenarioByProjectIDAndCreatInThisWeek(projectId, versionId);
        apiCountResult.setCreatedInWeek(dateCountByCreateInThisWeek);
        long executedInThisWeekCountNumber = apiScenarioReportService.countByProjectIdAndCreateInThisWeek(projectId, ExecutionExecuteTypeEnum.BASIC.name(), versionId);
        apiCountResult.setExecutedTimesInWeek(executedInThisWeekCountNumber);
        //所有执行次数
        long executedTimes = apiAutomationService.countExecuteTimesByProjectID(projectId, null, ExecutionExecuteTypeEnum.BASIC.name(), versionId);
        apiCountResult.setExecutedTimes(executedTimes);
        //未执行、未通过、已通过
        List<ApiDataCountResult> countResultByRunResult = apiAutomationService.countRunResultByProjectID(projectId, versionId);
        apiCountResult.countScenarioRunResult(countResultByRunResult);
        DecimalFormat df = new DecimalFormat("0.0");
        if (apiCountResult.getExecutedData() != 0) {
            //通过率
            float coveredRateNumber = (float) apiCountResult.getPassCount() * 100 / apiCountResult.getTotal();
            apiCountResult.setPassRate(df.format(coveredRateNumber) + "%");
            //执行率
            float executedRateNumber = (float) apiCountResult.getExecutedData() * 100 / apiCountResult.getTotal();
            apiCountResult.setExecutedRate(df.format(executedRateNumber) + "%");
        }

        return apiCountResult;
    }

    @GetMapping("/api/covered/{projectId}/{versionId}")
    public ApiDataCountDTO apiCovered(@PathVariable String projectId, @PathVariable String versionId) {
        versionId = this.initializationVersionId(versionId);
        ApiDataCountDTO apiCountResult = new ApiDataCountDTO();
        Map<String, List<ApiDefinition>> protocolAllDefinitionMap = apiDefinitionService.countEffectiveByProjectId(projectId, versionId);
        //统计覆盖率. 覆盖：接口下挂有用例/接口路径被场景引用
        //带有用例的接口
        List<ApiDefinition> apiDefinitionHasCase = apiDefinitionService.selectBaseInfoByProjectIdAndHasCase(projectId, versionId);
        //没有case的接口
        List<ApiDefinition> apiNoCaseList = apiDefinitionService.getAPiNotInCollection(protocolAllDefinitionMap, apiDefinitionHasCase);
        Map<String, Map<String, String>> scenarioUrlList = apiAutomationService.selectScenarioUseUrlByProjectId(projectId, null);
        List<String> apiIdInScenario = apiAutomationService.getApiIdInScenario(projectId, scenarioUrlList, apiNoCaseList);

        Map<String, List<ApiDefinition>> unCoverageApiMap = apiDefinitionService.getUnCoverageApiMap(apiNoCaseList, apiIdInScenario);
        Map<String, List<ApiDefinition>> coverageApiMap = apiDefinitionService.filterMap(protocolAllDefinitionMap, unCoverageApiMap);
        apiCountResult.countCovered(coverageApiMap, false);
        apiCountResult.countCovered(unCoverageApiMap, true);

        long total = apiCountResult.getCoveredCount() + apiCountResult.getNotCoveredCount();
        if (total > 0) {
            try {
                float coveredRateNumber = (float) apiCountResult.getCoveredCount() * 100 / total;
                DecimalFormat df = new DecimalFormat("0.0");
                apiCountResult.setApiCoveredRate(df.format(coveredRateNumber) + "%");
            } catch (Exception e) {
                LogUtil.error("转化通过率失败：[" + apiCountResult.getCoveredCount() + "，" + apiCountResult.getTotal() + "]", e);
            }
        }

        return apiCountResult;
    }

    @GetMapping("/api/case/covered/{projectId}/{versionId}")
    public ApiDataCountDTO caseCovered(@PathVariable String projectId, @PathVariable String versionId) throws Exception {
        versionId = this.initializationVersionId(versionId);
        ApiDataCountDTO apiCountResult = new ApiDataCountDTO();
        long allCaseCount = apiTestCaseService.countByProjectID(projectId, versionId);
        long allApiCount = apiDefinitionService.selectApiDefinitionBaseInfo(projectId, versionId).size();
        //两个大数据量下耗时比较长的查询同时进行
        CountDownLatch countDownLatch = new CountDownLatch(2);
        try {
            //未覆盖 已覆盖： 统计当前接口下是否含有案例
            List<ApiDataCountResult> countResultByApiCoverageList = apiDefinitionService.countApiCoverageByProjectID(projectId, versionId);
            apiCountResult.countApiCoverage(countResultByApiCoverageList);
            if (allApiCount != 0) {
                float coveredRateNumber = (float) apiCountResult.getCoveredCount() * 100 / allApiCount;
                DecimalFormat df = new DecimalFormat("0.0");
                apiCountResult.setApiCoveredRate(df.format(coveredRateNumber) + "%");
            }
        } finally {
            countDownLatch.countDown();
        }
        try {
            //计算用例的通过率和执行率
            List<ExecuteResultCountDTO> apiCaseExecResultList = apiTestCaseService.selectExecuteResultByProjectId(allCaseCount, projectId, versionId);
            apiCountResult.countApiCaseRunResult(apiCaseExecResultList);
            if (apiCountResult.getExecutedCount() > 0 && allCaseCount > 0) {
                //通过率
                float coveredRateNumber = (float) apiCountResult.getPassCount() * 100 / allCaseCount;
                DecimalFormat coveredRateFormat = new DecimalFormat("0.0");
                apiCountResult.setPassRate(coveredRateFormat.format(coveredRateNumber) + "%");
                //执行率
                float executedRateNumber = (float) apiCountResult.getExecutedData() * 100 / allCaseCount;
                DecimalFormat executedRateFormat = new DecimalFormat("0.0");
                apiCountResult.setExecutedRate(executedRateFormat.format(executedRateNumber) + "%");
            } else {
                apiCountResult.setPassRate("0%");
                apiCountResult.setExecutedRate("0%");
            }
        } finally {
            countDownLatch.countDown();
        }

        countDownLatch.await();
        return apiCountResult;
    }

    @GetMapping("/scenario/covered/{projectId}/{versionId}")
    public ApiDataCountDTO scenarioCovered(@PathVariable String projectId, @PathVariable String versionId) {
        versionId = this.initializationVersionId(versionId);
        ApiDataCountDTO apiCountResult = new ApiDataCountDTO();
        //统计覆盖率
        CoveredDTO coveredDTO = new CoveredDTO();
        Map<String, Map<String, String>> scenarioUrlList = apiAutomationService.selectScenarioUseUrlByProjectId(projectId, versionId);
        List<ApiDefinition> allEffectiveApiIdList = apiDefinitionService.selectEffectiveIdByProjectId(projectId, versionId);
        try {
            coveredDTO = apiAutomationService.countInterfaceCoverage(projectId, scenarioUrlList, allEffectiveApiIdList);
        } catch (Exception e) {
            LogUtil.error(e);
        }
        apiCountResult.setCoveredCount(coveredDTO.covered);
        apiCountResult.setNotCoveredCount(coveredDTO.notCovered);
        apiCountResult.setApiCoveredRate(coveredDTO.rateOfCovered);
        return apiCountResult;
    }

    @GetMapping("/schedule/task/count/{projectId}/{versionId}")
    public ApiDataCountDTO scheduleTaskCount(@PathVariable String projectId, @PathVariable String versionId) {
        versionId = this.initializationVersionId(versionId);
        List<Schedule> allScenarioScheduleList = baseScheduleService.selectScenarioTaskByProjectId(projectId, versionId);
        ApiDataCountDTO apiCountResult = new ApiDataCountDTO();
        long allTaskCount = allScenarioScheduleList.size();
        apiCountResult.setTotal(allTaskCount);
        long taskCountInThisWeek = baseScheduleService.countTaskByProjectIdInThisWeek(projectId, versionId);
        apiCountResult.setCreatedInWeek(taskCountInThisWeek);
        long executedInThisWeekCountNumber = apiScenarioReportService.countByProjectIdAndCreateAndByScheduleInThisWeek(projectId, ExecutionExecuteTypeEnum.BASIC.name(), versionId);
        apiCountResult.setExecutedTimesInWeek(executedInThisWeekCountNumber);
        long executedTimes = apiAutomationService.countExecuteTimesByProjectID(projectId, ReportTriggerMode.SCHEDULE.name(), ExecutionExecuteTypeEnum.BASIC.name(), versionId);
        apiCountResult.setExecutedTimes(executedTimes);
        //统计 失败 成功 以及总数
        List<ApiDataCountResult> allExecuteResult = apiScenarioReportService.countByProjectIdGroupByExecuteResult(projectId, ExecutionExecuteTypeEnum.BASIC.name(), versionId);
        apiCountResult.countScheduleExecute(allExecuteResult);
        if (executedTimes != 0) {
            float passRateNumber = (float) apiCountResult.getPassCount() * 100 / executedTimes;
            DecimalFormat df = new DecimalFormat("0.0");
            apiCountResult.setPassRate(df.format(passRateNumber) + "%");
        }

        //当前运行数、运行中、未运行
        long runningCount = 0;
        long notRunCount = 0;
        for (Schedule schedule : allScenarioScheduleList) {
            if (schedule.getEnable()) {
                runningCount++;
            } else {
                notRunCount++;
            }
        }
        apiCountResult.setRunningCount(runningCount);
        apiCountResult.setNotRunCount(notRunCount);
        return apiCountResult;
    }

    @PostMapping("/api/dubbo/providers")
    public List<DubboProvider> getProviders(@RequestBody RegistryCenter registry) {
        return apiDefinitionService.getProviders(registry);
    }

    @PostMapping(value = "/gen/performance/xml", consumes = {"multipart/form-data"})
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_CREATE_PERFORMANCE)
    public ScenarioToPerformanceInfoDTO genPerformanceTest(@RequestPart("request") RunDefinitionRequest runRequest, @RequestPart(value = "files", required = false) List<MultipartFile> bodyFiles) {
        JmxInfoDTO jmxInfoDTO = DataFormattingUtil.getJmxInfoDTO(runRequest, bodyFiles);
        ScenarioToPerformanceInfoDTO returnDTO = new ScenarioToPerformanceInfoDTO();
        returnDTO.setJmxInfoDTO(jmxInfoDTO);
        Map<String, List<String>> projectEnvMap = apiDefinitionService.selectEnvironmentByHashTree(runRequest.getProjectId(), runRequest.getTestElement());
        returnDTO.setProjectEnvMap(projectEnvMap);
        return returnDTO;
    }

    @PostMapping("/runningTask/{projectId}/{versionId}/{goPage}/{pageSize}")
    public Pager<List<TaskInfoResult>> runningTask(@PathVariable String projectId, @PathVariable String versionId, @PathVariable int goPage, @PathVariable int pageSize) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        versionId = this.initializationVersionId(versionId);
        List<TaskInfoResult> resultList = taskService.findScenarioAndSwaggerRunningTaskInfoByProjectID(projectId, versionId);
        int dataIndex = 1;
        for (TaskInfoResult taskInfo :
                resultList) {
            taskInfo.setIndex(dataIndex++);
            Date nextExecutionTime = CronUtils.getNextTriggerTime(taskInfo.getRule());
            if (nextExecutionTime != null) {
                taskInfo.setNextExecutionTime(nextExecutionTime.getTime());
            }
        }
        return PageUtils.setPageInfo(page, resultList);
    }

    //初始化版本ID
    private String initializationVersionId(String versionId) {
        if (StringUtils.equalsIgnoreCase(versionId, "default")) {
            return null;
        } else {
            return versionId;
        }
    }
}