package io.metersphere.controller.home;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.DubboProvider;
import io.metersphere.api.dto.JmxInfoDTO;
import io.metersphere.api.dto.RegistryCenter;
import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.dto.datacount.ExecutedCaseInfoResult;
import io.metersphere.api.dto.datacount.response.ApiDataCountDTO;
import io.metersphere.api.dto.datacount.response.CoveredDTO;
import io.metersphere.api.dto.datacount.response.ExecuteResultCountDTO;
import io.metersphere.api.dto.datacount.response.ExecutedCaseInfoDTO;
import io.metersphere.api.dto.definition.RunDefinitionRequest;
import io.metersphere.api.dto.export.ScenarioToPerformanceInfoDTO;
import io.metersphere.base.domain.ApiDefinition;
import io.metersphere.base.domain.Schedule;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
        List<ApiDataCountResult> countResultByProtocolList = apiDefinitionService.countProtocolByProjectID(projectId, versionId);
        apiCountResult.countProtocol(countResultByProtocolList);
        long dateCountByCreateInThisWeek = apiDefinitionService.countByProjectIDAndCreateInThisWeek(projectId, versionId);
        apiCountResult.setCreatedInWeek(dateCountByCreateInThisWeek);
        //查询完成率、进行中、已完成
        List<ApiDataCountResult> countResultByStateList = apiDefinitionService.countStateByProjectID(projectId, versionId);
        apiCountResult.countStatus(countResultByStateList);
        long allCount = apiCountResult.getFinishedCount() + apiCountResult.getRunningCount() + apiCountResult.getNotStartedCount();
        if (allCount != 0) {
            float completeRateNumber = (float) apiCountResult.getFinishedCount() * 100 / allCount;
            DecimalFormat df = new DecimalFormat("0.0");
            apiCountResult.setCompletedRate(df.format(completeRateNumber) + "%");
        }
        //统计覆盖率
        long effectiveApiCount = apiDefinitionService.countEffectiveByProjectId(projectId, versionId);
        long apiHasCase = apiDefinitionService.countApiByProjectIdAndHasCase(projectId, versionId);
        List<ApiDefinition> apiNoCaseList = apiDefinitionService.selectEffectiveIdByProjectIdAndHaveNotCase(projectId, versionId);
        Map<String, Map<String, String>> scenarioUrlList = apiAutomationService.selectScenarioUseUrlByProjectId(projectId, null);
        int apiInScenario = apiAutomationService.getApiIdInScenario(projectId, scenarioUrlList, apiNoCaseList).size();

        if (effectiveApiCount == 0) {
            apiCountResult.setCoveredCount(0);
            apiCountResult.setNotCoveredCount(0);
        } else {
            long quotedApiCount = apiHasCase + apiInScenario;
            apiCountResult.setCoveredCount(quotedApiCount);
            apiCountResult.setNotCoveredCount(effectiveApiCount - quotedApiCount);
            try {
                float coveredRateNumber = (float) quotedApiCount * 100 / effectiveApiCount;
                DecimalFormat df = new DecimalFormat("0.0");
                apiCountResult.setApiCoveredRate(df.format(coveredRateNumber) + "%");
            } catch (Exception e) {
                LogUtil.error("转化通过率失败：[" + quotedApiCount + "，" + effectiveApiCount + "]", e);
            }
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
        //未覆盖 已覆盖： 统计当前接口下是否含有案例
        List<ApiDataCountResult> countResultByApiCoverageList = apiDefinitionService.countApiCoverageByProjectID(projectId, versionId);
        apiCountResult.countApiCoverage(countResultByApiCoverageList);
        long allCount = apiCountResult.getCoveredCount() + apiCountResult.getNotCoveredCount();
        if (allCount != 0) {
            float coveredRateNumber = (float) apiCountResult.getCoveredCount() * 100 / allCount;
            DecimalFormat df = new DecimalFormat("0.0");
            apiCountResult.setApiCoveredRate(df.format(coveredRateNumber) + "%");
        }
        //计算用例的通过率和执行率
        List<ExecuteResultCountDTO> apiCaseExecResultList = apiTestCaseService.selectExecuteResultByProjectId(projectId, versionId);
        apiCountResult.countApiCaseRunResult(apiCaseExecResultList);
        if (apiCountResult.getExecutedCount() > 0) {
            //通过率
            float coveredRateNumber = (float) apiCountResult.getPassCount() * 100 / apiCountResult.getTotal();
            DecimalFormat coveredRateFormat = new DecimalFormat("0.0");
            apiCountResult.setPassRate(coveredRateFormat.format(coveredRateNumber) + "%");

            float executedRateNumber = (float) apiCountResult.getExecutedData() * 100 / apiCountResult.getTotal();
            DecimalFormat executedRateFormat = new DecimalFormat("0.0");
            apiCountResult.setExecutedRate(executedRateFormat.format(executedRateNumber) + "%");
        } else {
            apiCountResult.setPassRate("0%");
            apiCountResult.setExecutedRate("0%");
        }
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
        List<ApiDataCountResult> allExecuteResult = apiScenarioReportService.countByProjectIdGroupByExecuteResult(projectId, versionId);
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


    @GetMapping("/failure/case/about/plan/{projectId}/{versionId}/{selectFunctionCase}/{limitNumber}/{goPage}/{pageSize}")
    public Pager<List<ExecutedCaseInfoDTO>> failureCaseAboutTestPlan(@PathVariable String projectId, @PathVariable String versionId, @PathVariable boolean selectFunctionCase,
                                                                     @PathVariable int limitNumber, @PathVariable int goPage, @PathVariable int pageSize) {
        versionId = this.initializationVersionId(versionId);
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        List<ExecutedCaseInfoResult> selectDataList = apiDefinitionExecResultService.findFailureCaseInfoByProjectIDAndLimitNumberInSevenDays(projectId, versionId, selectFunctionCase, limitNumber);
        List<ExecutedCaseInfoDTO> returnList = new ArrayList<>(selectDataList.size());
        for (int dataIndex = 0; dataIndex < selectDataList.size(); dataIndex++) {
            ExecutedCaseInfoDTO dataDTO = new ExecutedCaseInfoDTO();
            dataDTO.setSortIndex(dataIndex + 1);
            ExecutedCaseInfoResult selectData = selectDataList.get(dataIndex);
            dataDTO.setCaseID(selectData.getTestCaseID());
            dataDTO.setCaseName(selectData.getCaseName());
            dataDTO.setTestPlan(selectData.getTestPlan());
            dataDTO.setFailureTimes(selectData.getFailureTimes());
            dataDTO.setTestPlanId(selectData.getTestPlanId());
            dataDTO.setCaseType(selectData.getCaseType());
            dataDTO.setId(selectData.getId());
            dataDTO.setTestPlanDTOList(selectData.getTestPlanDTOList());
            returnList.add(dataDTO);
        }
        return PageUtils.setPageInfo(page, returnList);
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