package io.metersphere.controller.home;

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
import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.utils.DataFormattingUtil;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.service.BaseScheduleService;
import io.metersphere.service.definition.ApiDefinitionExecResultService;
import io.metersphere.service.definition.ApiDefinitionService;
import io.metersphere.service.definition.ApiTestCaseService;
import io.metersphere.service.scenario.ApiScenarioReportService;
import io.metersphere.service.scenario.ApiScenarioService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.text.DecimalFormat;
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


    @GetMapping("/api/count/{projectId}")
    public ApiDataCountDTO apiCount(@PathVariable String projectId) {
        ApiDataCountDTO apiCountResult = new ApiDataCountDTO();
        List<ApiDataCountResult> countResultByProtocolList = apiDefinitionService.countProtocolByProjectID(projectId);
        apiCountResult.countProtocol(countResultByProtocolList);
        long dateCountByCreateInThisWeek = apiDefinitionService.countByProjectIDAndCreateInThisWeek(projectId);
        apiCountResult.setCreatedInWeek(dateCountByCreateInThisWeek);
        //查询完成率、进行中、已完成
        List<ApiDataCountResult> countResultByStateList = apiDefinitionService.countStateByProjectID(projectId);
        apiCountResult.countStatus(countResultByStateList);
        long allCount = apiCountResult.getFinishedCount() + apiCountResult.getRunningCount() + apiCountResult.getNotStartedCount();
        if (allCount != 0) {
            float completeRateNumber = (float) apiCountResult.getFinishedCount() * 100 / allCount;
            DecimalFormat df = new DecimalFormat("0.0");
            apiCountResult.setCompletedRate(df.format(completeRateNumber) + "%");
        }
        //统计覆盖率
        long effectiveApiCount = apiDefinitionService.countEffectiveByProjectId(projectId);
        long apiHasCase = apiDefinitionService.countApiByProjectIdAndHasCase(projectId);
        List<ApiDefinition> apiNoCaseList = apiDefinitionService.selectEffectiveIdByProjectIdAndHaveNotCase(projectId);
        Map<String, Map<String, String>> scenarioUrlList = apiAutomationService.selectScenarioUseUrlByProjectId(projectId);
        int apiInScenario = apiAutomationService.getApiIdInScenario(projectId, scenarioUrlList, apiNoCaseList).size();

        try {
            if (effectiveApiCount == 0) {
                apiCountResult.setCoveredCount(0);
                apiCountResult.setNotCoveredCount(0);
            } else {
                long quotedApiCount = apiHasCase + apiInScenario;
                apiCountResult.setCoveredCount(quotedApiCount);
                apiCountResult.setNotCoveredCount(effectiveApiCount - quotedApiCount);
                float coveredRateNumber = (float) quotedApiCount * 100 / effectiveApiCount;
                DecimalFormat df = new DecimalFormat("0.0");
                apiCountResult.setApiCoveredRate(df.format(coveredRateNumber) + "%");
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }

        return apiCountResult;
    }

    @GetMapping("/api/case/count/{projectId}")
    public ApiDataCountDTO apiCaseCount(@PathVariable String projectId) {
        ApiDataCountDTO apiCountResult = new ApiDataCountDTO();
        List<ApiDataCountResult> countResultList = apiTestCaseService.countProtocolByProjectID(projectId);
        apiCountResult.countProtocol(countResultList);
        //本周创建、本周执行、总执行
        long dateCountByCreateInThisWeek = apiTestCaseService.countByProjectIDAndCreateInThisWeek(projectId);
        apiCountResult.setCreatedInWeek(dateCountByCreateInThisWeek);
        long executedInThisWeekCountNumber = apiDefinitionExecResultService.countByTestCaseIDInProjectAndExecutedInThisWeek(projectId);
        apiCountResult.setExecutedTimesInWeek(executedInThisWeekCountNumber);
        long executedCount = apiTestCaseService.countExecutedTimesByProjectId(projectId);
        apiCountResult.setExecutedCount(executedCount);
        //未覆盖 已覆盖： 统计当前接口下是否含有案例
        List<ApiDataCountResult> countResultByApiCoverageList = apiDefinitionService.countApiCoverageByProjectID(projectId);
        apiCountResult.countApiCoverage(countResultByApiCoverageList);
        long allCount = apiCountResult.getCoveredCount() + apiCountResult.getNotCoveredCount();
        if (allCount != 0) {
            float coveredRateNumber = (float) apiCountResult.getCoveredCount() * 100 / allCount;
            DecimalFormat df = new DecimalFormat("0.0");
            apiCountResult.setApiCoveredRate(df.format(coveredRateNumber) + "%");
        }
        //计算用例的通过率和执行率
        List<ExecuteResultCountDTO> apiCaseExecResultList = apiTestCaseService.selectExecuteResultByProjectId(projectId);
        apiCountResult.countApiCaseRunResult(apiCaseExecResultList);
        if (apiCountResult.getExecutedCount() > 0) {
            //通过率
            float coveredRateNumber = (float) apiCountResult.getPassCount() * 100 / apiCountResult.getExecutedCount();
            DecimalFormat coveredRateFormat = new DecimalFormat("0.0");
            apiCountResult.setPassRate(coveredRateFormat.format(coveredRateNumber) + "%");

            float executedRateNumber = (float) apiCountResult.getExecutedCount() * 100 / apiCountResult.getTotal();
            DecimalFormat executedRateFormat = new DecimalFormat("0.0");
            apiCountResult.setExecutedRate(executedRateFormat.format(executedRateNumber) + "%");
        } else {
            apiCountResult.setPassRate("0%");
            apiCountResult.setExecutedRate("0%");
        }
        return apiCountResult;
    }

    @GetMapping("/scenario/count/{projectId}")
    public ApiDataCountDTO scenarioCount(@PathVariable String projectId) {
        ApiDataCountDTO apiCountResult = new ApiDataCountDTO();
        long scenarioCountNumber = apiAutomationService.countScenarioByProjectID(projectId);
        apiCountResult.setTotal(scenarioCountNumber);
        //统计覆盖率
        long dateCountByCreateInThisWeek = apiAutomationService.countScenarioByProjectIDAndCreatInThisWeek(projectId);
        apiCountResult.setCreatedInWeek(dateCountByCreateInThisWeek);
        long executedInThisWeekCountNumber = apiScenarioReportService.countByProjectIdAndCreateInThisWeek(projectId);
        apiCountResult.setExecutedTimesInWeek(executedInThisWeekCountNumber);
        long executedCount = apiAutomationService.countExecuteTimesByProjectID(projectId, null);
        apiCountResult.setExecutedCount(executedCount);
        //未执行、未通过、已通过
        List<ApiDataCountResult> countResultByRunResult = apiAutomationService.countRunResultByProjectID(projectId);
        apiCountResult.countScenarioRunResult(countResultByRunResult);
        DecimalFormat df = new DecimalFormat("0.0");
        if (executedCount != 0) {
            //通过率
            float coveredRateNumber = (float) apiCountResult.getPassCount() * 100 / executedCount;
            apiCountResult.setPassRate(df.format(coveredRateNumber) + "%");
            //执行率
            float executedRateNumber = (float) apiCountResult.getExecutedCount() * 100 / apiCountResult.getTotal();
            apiCountResult.setExecutedRate(df.format(executedRateNumber) + "%");
        }

        //统计覆盖率
        CoveredDTO coveredDTO = new CoveredDTO();
        Map<String, Map<String, String>> scenarioUrlList = apiAutomationService.selectScenarioUseUrlByProjectId(projectId);
        List<ApiDefinition> allEffectiveApiIdList = apiDefinitionService.selectEffectiveIdByProjectId(projectId);
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

    @GetMapping("/schedule/task/count/{projectId}")
    public ApiDataCountDTO scheduleTaskCount(@PathVariable String projectId) {
        List<Schedule> allScenarioScheduleList = baseScheduleService.selectScenarioTaskByProjectId(projectId);

        ApiDataCountDTO apiCountResult = new ApiDataCountDTO();
        long allTaskCount = allScenarioScheduleList.size();
        apiCountResult.setTotal(allTaskCount);
        long taskCountInThisWeek = baseScheduleService.countTaskByProjectIdInThisWeek(projectId);
        apiCountResult.setCreatedInWeek(taskCountInThisWeek);
        long executedInThisWeekCountNumber = apiScenarioReportService.countByProjectIdAndCreateAndByScheduleInThisWeek(projectId);
        apiCountResult.setExecutedTimesInWeek(executedInThisWeekCountNumber);
        long executedCount = apiAutomationService.countExecuteTimesByProjectID(projectId, ReportTriggerMode.SCHEDULE.name());
        apiCountResult.setExecutedCount(executedCount);
        //统计 失败 成功 以及总数
        List<ApiDataCountResult> allExecuteResult = apiScenarioReportService.countByProjectIdGroupByExecuteResult(projectId);
        apiCountResult.countScheduleExecute(allExecuteResult);
        long allCount = apiCountResult.getExecutedCount();
        if (allCount != 0) {
            float coveredRateNumber = (float) apiCountResult.getPassCount() * 100 / allCount;
            DecimalFormat df = new DecimalFormat("0.0");
            apiCountResult.setPassRate(df.format(coveredRateNumber) + "%");
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
}