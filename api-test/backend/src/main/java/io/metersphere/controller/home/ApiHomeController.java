package io.metersphere.controller.home;

import io.metersphere.api.dto.DubboProvider;
import io.metersphere.api.dto.JmxInfoDTO;
import io.metersphere.api.dto.RegistryCenter;
import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.dto.datacount.ExecutedCaseInfoResult;
import io.metersphere.api.dto.datacount.response.ApiDataCountDTO;
import io.metersphere.api.dto.datacount.response.CoverageDTO;
import io.metersphere.api.dto.datacount.response.ExecuteResultCountDTO;
import io.metersphere.api.dto.datacount.response.ExecutedCaseInfoDTO;
import io.metersphere.api.dto.definition.RunDefinitionRequest;
import io.metersphere.api.dto.export.ScenarioToPerformanceInfoDTO;
import io.metersphere.service.scenario.ApiScenarioService;
import io.metersphere.service.definition.ApiDefinitionExecResultService;
import io.metersphere.service.definition.ApiDefinitionService;
import io.metersphere.service.scenario.ApiScenarioReportService;
import io.metersphere.service.definition.ApiTestCaseService;
import io.metersphere.base.domain.ApiDefinition;
import io.metersphere.base.mapper.ext.ExtScheduleMapper;
import io.metersphere.commons.enums.ApiReportStatus;
import io.metersphere.commons.enums.ApiTestDataStatus;
import io.metersphere.commons.utils.CronUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.TaskInfoResult;
import io.metersphere.request.BaseQueryRequest;
import io.metersphere.service.BaseScheduleService;
import io.metersphere.commons.utils.DataFormattingUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
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
    private ExtScheduleMapper extScheduleMapper;


    @GetMapping("/api/count/{projectId}")
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

    /**
     * 接口覆盖率统计
     *
     * @param projectId
     * @return
     */
    @GetMapping("/api/coverage/{projectId}")
    public CoverageDTO countApiCoverage(@PathVariable String projectId) {
        CoverageDTO coverage = new CoverageDTO();
        /**
         * 接口覆盖率
         * 有案例的接口/没有案例确被场景引用  （apiHasCase + apiInScenario） ： 所有的接口（effectiveApiCount）
         */
        long effectiveApiCount = apiDefinitionService.countEffectiveByProjectId(projectId);
        long apiHasCase = apiDefinitionService.countApiByProjectIdAndHasCase(projectId);


        /**
         * 计算没有用例接口的覆盖数量
         */
        List<ApiDefinition> apiNoCaseList = apiDefinitionService.selectEffectiveIdByProjectIdAndHaveNotCase(projectId);
        Map<String, Map<String, String>> scenarioUrlList = apiAutomationService.selectScenarioUseUrlByProjectId(projectId);
        int apiInScenario = apiAutomationService.getApiIdInScenario(projectId, scenarioUrlList, apiNoCaseList).size();

        try {
            if (effectiveApiCount == 0) {
                coverage.setCoverate(0);
                coverage.setNotCoverate(0);
            } else {
                long quotedApiCount = apiHasCase + apiInScenario;
                coverage.setCoverate(quotedApiCount);
                coverage.setNotCoverate(effectiveApiCount - quotedApiCount);
                float coverageRageNumber = (float) quotedApiCount * 100 / effectiveApiCount;
                DecimalFormat df = new DecimalFormat("0.0");
                coverage.setRateOfCoverage(df.format(coverageRageNumber) + "%");
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return coverage;
    }

    /**
     * 场景覆盖率统计
     *
     * @param projectId
     * @return
     */
    @GetMapping("/scenario/coverage/{projectId}")
    public CoverageDTO countScenarioCoverage(@PathVariable String projectId) {
        CoverageDTO coverage = new CoverageDTO();
        /**
         * 接口覆盖率
         * 复制的接口定义/复制或引用的单接口用例/ 添加的自定义请求 url 路径与现有的接口定义一致的请求
         */
        Map<String, Map<String, String>> scenarioUrlList = apiAutomationService.selectScenarioUseUrlByProjectId(projectId);
        List<ApiDefinition> allEffectiveApiIdList = apiDefinitionService.selectEffectiveIdByProjectId(projectId);
        try {
            coverage = apiAutomationService.countInterfaceCoverage(projectId, scenarioUrlList, allEffectiveApiIdList);
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return coverage;
    }

    @GetMapping("/api/case/count/{projectId}")
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
        //计算用例的通过率
        List<ExecuteResultCountDTO> apiCaseExecResultList = apiTestCaseService.selectExecuteResultByProjectId(projectId);
        long unexecuteCount = 0;
        long executionFailedCount = 0;
        long executionPassCount = 0;
        long fakeErrorCount = 0;
        for (ExecuteResultCountDTO execResult : apiCaseExecResultList) {
            if (StringUtils.isEmpty(execResult.getExecResult())) {
                unexecuteCount += execResult.getCount();
            } else if (StringUtils.equalsAnyIgnoreCase(execResult.getExecResult(), ApiTestDataStatus.UNDERWAY.getValue(),
                    ApiReportStatus.STOPPED.name())) {
                unexecuteCount += execResult.getCount();
            } else if (StringUtils.equalsIgnoreCase(execResult.getExecResult(), ApiReportStatus.SUCCESS.name())) {
                executionPassCount += execResult.getCount();
            } else if (StringUtils.equalsAnyIgnoreCase(execResult.getExecResult(), ApiReportStatus.FAKE_ERROR.name())) {
                fakeErrorCount += execResult.getCount();
            } else {
                executionFailedCount += execResult.getCount();
            }
        }
        apiCountResult.setUnexecuteCount(unexecuteCount);
        apiCountResult.setExecutionFailedCount(executionFailedCount);
        apiCountResult.setExecutionPassCount(executionPassCount);
        apiCountResult.setFakeErrorCount(fakeErrorCount);

        if (unexecuteCount + executionFailedCount + executionPassCount + fakeErrorCount > 0) {
            //通过率
            float coverageRageNumber = (float) executionPassCount * 100 / (unexecuteCount + executionFailedCount + fakeErrorCount + executionPassCount);
            DecimalFormat df = new DecimalFormat("0.0");
            apiCountResult.setPassRage(df.format(coverageRageNumber) + "%");
        } else {
            apiCountResult.setPassRage("0%");
        }
        return apiCountResult;
    }

    @GetMapping("/scenario/count/{projectId}")
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
        long executedCountNumber = apiAutomationService.countExecuteTimesByProjectID(projectId);
        apiCountResult.setExecutedCount(executedCountNumber);

        //未执行、未通过、已通过
        List<ApiDataCountResult> countResultByRunResult = apiAutomationService.countRunResultByProjectID(projectId);
        apiCountResult.countRunResult(countResultByRunResult);
        long allCount = apiCountResult.getAllExecutedResultCount();
        DecimalFormat df = new DecimalFormat("0.0");
        if (allCount != 0) {
            //通过率
            float coverageRageNumber = (float) apiCountResult.getExecutionPassCount() * 100 / allCount;
            apiCountResult.setPassRage(df.format(coverageRageNumber) + "%");
        }
        return apiCountResult;
    }

    @GetMapping("/schedule/task/count/{projectId}")
    public ApiDataCountDTO scheduleTaskInfoCount(@PathVariable String projectId) {
        ApiDataCountDTO apiCountResult = new ApiDataCountDTO();

        long allTaskCount = baseScheduleService.countTaskByProjectId(projectId);

        apiCountResult.setAllApiDataCountNumber(allTaskCount);

        long taskCountInThisWeek = baseScheduleService.countTaskByProjectIdInThisWeek(projectId);
        apiCountResult.setThisWeekAddedCount(taskCountInThisWeek);
        long executedInThisWeekCountNumber = apiScenarioReportService.countByProjectIdAndCreateAndByScheduleInThisWeek(projectId);
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

    public List<TaskInfoResult> findRunningTaskInfoByProjectID(String projectID, BaseQueryRequest request) {
        List<TaskInfoResult> runningTaskInfoList = extScheduleMapper.findRunningTaskInfoByProjectID(projectID, request);
        return runningTaskInfoList;
    }

    @PostMapping("/running/task/{projectID}")
    public List<TaskInfoResult> runningTask(@PathVariable String projectID, @RequestBody BaseQueryRequest request) {
        List<TaskInfoResult> resultList = this.findRunningTaskInfoByProjectID(projectID, request);
        int dataIndex = 1;
        for (TaskInfoResult taskInfo : resultList) {
            taskInfo.setIndex(dataIndex++);
            Date nextExecutionTime = CronUtils.getNextTriggerTime(taskInfo.getRule());
            if (nextExecutionTime != null) {
                taskInfo.setNextExecutionTime(nextExecutionTime.getTime());
            }
        }
        return resultList;
    }

    @PostMapping("/api/dubbo/providers")
    public List<DubboProvider> getProviders(@RequestBody RegistryCenter registry) {
        return apiDefinitionService.getProviders(registry);
    }

    @PostMapping(value = "/gen/performance/xml", consumes = {"multipart/form-data"})
    public ScenarioToPerformanceInfoDTO genPerformanceTest(@RequestPart("request") RunDefinitionRequest runRequest, @RequestPart(value = "files", required = false) List<MultipartFile> bodyFiles) throws Exception {
        JmxInfoDTO jmxInfoDTO = DataFormattingUtil.getJmxInfoDTO(runRequest, bodyFiles);
        ScenarioToPerformanceInfoDTO returnDTO = new ScenarioToPerformanceInfoDTO();
        returnDTO.setJmxInfoDTO(jmxInfoDTO);
        Map<String, List<String>> projectEnvMap = apiDefinitionService.selectEnvironmentByHashTree(runRequest.getProjectId(), runRequest.getTestElement());
        returnDTO.setProjectEnvMap(projectEnvMap);
        return returnDTO;
    }

    @GetMapping("/failure/case/about/plan/{projectId}/{selectFunctionCase}/{limitNumber}")
    public List<ExecutedCaseInfoDTO> failureCaseAboutTestPlan(@PathVariable String projectId, @PathVariable boolean selectFunctionCase, @PathVariable int limitNumber) {
        List<ExecutedCaseInfoResult> selectDataList = apiDefinitionExecResultService.findFailureCaseInfoByProjectIDAndLimitNumberInSevenDays(projectId, selectFunctionCase, limitNumber);
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
                dataDTO.setTestPlanId(selectData.getTestPlanId());
                dataDTO.setCaseType(selectData.getCaseType());
                dataDTO.setId(selectData.getId());
                dataDTO.setTestPlanDTOList(selectData.getTestPlanDTOList());
            } else {
                dataDTO.setCaseName(StringUtils.EMPTY);
                dataDTO.setTestPlan(StringUtils.EMPTY);
            }
            returnList.add(dataDTO);
        }
        return returnList;
    }
}