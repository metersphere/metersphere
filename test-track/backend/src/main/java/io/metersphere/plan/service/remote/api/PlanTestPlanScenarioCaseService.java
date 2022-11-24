package io.metersphere.plan.service.remote.api;

import io.metersphere.base.domain.TestPlanReport;
import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.*;
import io.metersphere.plan.constant.ApiReportStatus;
import io.metersphere.plan.dto.*;
import io.metersphere.plan.request.api.ApiPlanReportRequest;
import io.metersphere.plan.request.api.ApiScenarioRequest;
import io.metersphere.plan.service.TestPlanService;
import io.metersphere.plan.utils.TestPlanStatusCalculator;
import io.metersphere.utils.DiscoveryUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlanTestPlanScenarioCaseService extends ApiTestService {

    private static final String BASE_UEL = "/test/plan/scenario/case";

    @Resource
    @Lazy
    TestPlanService testPlanService;

    @Resource
    PlanApiScenarioReportService planApiScenarioReportService;

    public void calculatePlanReport(String planId, TestPlanSimpleReportDTO report) {
        if (DiscoveryUtil.hasService(MicroServiceName.API_TEST)) {
            List<PlanReportCaseDTO> planReportCaseDTOS = selectStatusForPlanReport(planId);
            calculatePlanReport(report, planReportCaseDTOS);
            //记录接口用例的运行环境信息
            List<String> idList = planReportCaseDTOS.stream().map(PlanReportCaseDTO::getId).collect(Collectors.toList());
            try {
                report.setProjectEnvMap(getPlanProjectEnvMap(idList));
            } catch (Exception e) {
                LogUtil.error(e);
            }
        }
    }

    public void calculatePlanReport(List<String> reportIds, TestPlanSimpleReportDTO report) {
        try {
            List<PlanReportCaseDTO> planReportCaseDTOS = planApiScenarioReportService.selectForPlanReport(reportIds);
            calculatePlanReport(report, planReportCaseDTOS);
        } catch (MSException e) {
            LogUtil.error(e);
        }
    }

    public void calculatePlanReport(TestPlanSimpleReportDTO report, List<PlanReportCaseDTO> planReportCaseDTOS) {
        TestPlanApiResultReportDTO apiResult = report.getApiResult();
        List<TestCaseReportStatusResultDTO> statusResult = new ArrayList<>();
        Map<String, TestCaseReportStatusResultDTO> statusResultMap = new HashMap<>();
        TestPlanStatusCalculator.buildStatusResultMap(planReportCaseDTOS, statusResultMap, report, ApiReportStatus.SUCCESS.name());
        TestPlanStatusCalculator.addToReportCommonStatusResultList(statusResultMap, statusResult);

        TestPlanScenarioStepCountSimpleDTO stepCountResult = getStepCount(planReportCaseDTOS);

        TestPlanScenarioStepCountDTO stepCount = stepCountResult.getStepCount();
        int underwayStepsCounts = stepCountResult.getUnderwayStepsCounts();

        List<TestCaseReportStatusResultDTO> stepResult = getStepResult(stepCount, underwayStepsCounts);
        apiResult.setApiScenarioData(statusResult);
        apiResult.setApiScenarioStepData(stepResult);
        report.setApiResult(apiResult);
    }

    @NotNull
    public List<TestCaseReportStatusResultDTO> getStepResult(TestPlanScenarioStepCountDTO stepCount, int underwayStepsCounts) {
        List<TestCaseReportStatusResultDTO> stepResult = new ArrayList<>();
        getScenarioCaseReportStatusResultDTO(ApiReportStatus.ERROR.name(), stepCount.getScenarioStepError(), stepResult);
        getScenarioCaseReportStatusResultDTO(ApiReportStatus.SUCCESS.name(), stepCount.getScenarioStepSuccess(), stepResult);
        getScenarioCaseReportStatusResultDTO(ApiReportStatus.FAKE_ERROR.name(), stepCount.getScenarioStepErrorReport(), stepResult);
        getScenarioCaseReportStatusResultDTO(ApiReportStatus.PENDING.name(),
                stepCount.getScenarioStepTotal() - stepCount.getScenarioStepSuccess() - stepCount.getScenarioStepError() - stepCount.getScenarioStepErrorReport() + underwayStepsCounts, stepResult);
        return stepResult;
    }

    private void getScenarioCaseReportStatusResultDTO(String status, int count, List<TestCaseReportStatusResultDTO> scenarioCaseList) {
        if (count > 0) {
            TestCaseReportStatusResultDTO scenarioCase = new TestCaseReportStatusResultDTO();
            scenarioCase.setStatus(status);
            scenarioCase.setCount(count);
            scenarioCaseList.add(scenarioCase);
        }
    }

    private TestPlanScenarioStepCountSimpleDTO getStepCount(List<PlanReportCaseDTO> planReportCaseDTOS) {
        return microService.postForData(serviceName, BASE_UEL + "/step/count", planReportCaseDTOS, TestPlanScenarioStepCountSimpleDTO.class);
    }

    public List<PlanReportCaseDTO> selectStatusForPlanReport(String planId) {
        return microService.getForDataArray(serviceName, BASE_UEL + "/get/report/status/" + planId, PlanReportCaseDTO.class);
    }

    public Map<String, List<String>> getPlanProjectEnvMap(List<String> resourceIds) {
        return (Map<String, List<String>>) microService.postForData(serviceName, BASE_UEL + "/get/plan/env/map", resourceIds);
    }

    public List<String> getExecResultByPlanId(String planId) {
        return microService.getForDataArray(serviceName, BASE_UEL + "/plan/exec/result/" + planId, String.class);
    }

    public List<ApiScenarioDTO> listByPlanId(String planId) {
        return microService.getForDataArray(serviceName, BASE_UEL + "/list/" + planId, ApiScenarioDTO.class);
    }

    public RunModeConfigDTO setScenarioEnv(String planId, RunModeConfigDTO runModeConfig) {
       return microService.postForData(serviceName, BASE_UEL + "/set/env/" + planId, runModeConfig, RunModeConfigDTO.class);
    }

    public void relevanceByTestIds(List<String> scenarioIds, String planId) {
        microService.postForData(serviceName, BASE_UEL + "/relevance/" + planId, scenarioIds);
    }

    public void copyPlan(String sourcePlanId, String targetPlanId) {
        microService.getForData(serviceName, BASE_UEL + "/plan/copy/" + sourcePlanId + "/" + targetPlanId);
    }

    public boolean haveExecCase(String planId) {
        try {
            return microService.getForData(serviceName, BASE_UEL + "/have/exec/" + planId, Boolean.class);
        } catch (MSException e) {
            LogUtil.error(e);
            return false;
        }
    }

    public Map<String, List<String>> getApiCaseEnv(List<String> planApiScenarioIds) {
        return microService.postForData(serviceName, BASE_UEL + "/get/env", planApiScenarioIds, Map.class);
    }

    public Map<String, List<String>> getApiScenarioEnv(String planId) {
        return microService.getForData(serviceName, BASE_UEL + "/get/env/" + planId, Map.class);
    }

    public ApiPlanReportDTO getApiReport(ApiPlanReportRequest request) {
        return microService.postForData(serviceName, BASE_UEL + "/plan/report", request, ApiPlanReportDTO.class);
    }

    public ApiPlanReportDTO getApiExecuteReport(ApiPlanReportRequest request) {
        return microService.postForData(serviceName, BASE_UEL + "/plan/execute/report", request, ApiPlanReportDTO.class);
    }

    public TestPlanApiReportInfoDTO genApiReportInfoForSchedule(String planId, RunModeConfigDTO runModeConfigDTO) {
        return microService.postForData(serviceName, BASE_UEL + "/plan/report/schedule/info/" + planId, runModeConfigDTO, TestPlanApiReportInfoDTO.class);
    }

    public Boolean isCaseExecuting(String planId) {
        return microService.getForData(serviceName, BASE_UEL + "/is/executing/" + planId, Boolean.class);
    }

    public List<TestPlanFailureScenarioDTO> getFailureListByIds(Set<String> ids) {
        return microService.postForDataArray(serviceName, BASE_UEL + "/failure/list", ids, TestPlanFailureScenarioDTO.class);
    }

    public TestPlanEnvInfoDTO generateEnvironmentInfo(TestPlanReport testPlanReport) {
        return microService.postForData(serviceName, BASE_UEL + "/env/generate", testPlanReport, TestPlanEnvInfoDTO.class);
    }

    public Boolean hasFailCase(String planId, List<String> automationIds) {
        return (Boolean) microService.postForData(serviceName, BASE_UEL + "/has/fail/" + planId, automationIds);
    }

    public List<ApiScenarioModuleDTO> getNodeByPlanId(List<String> projectIds, String planId) {
        return microService.postForDataArray(serviceName, BASE_UEL + "/list/module/" + planId, projectIds, ApiScenarioModuleDTO.class);
    }

    public List<TestPlanFailureScenarioDTO> buildResponse(List<TestPlanFailureScenarioDTO> scenarioCases) {
        return microService.postForDataArray(serviceName, BASE_UEL + "/build/response", scenarioCases, TestPlanFailureScenarioDTO.class);
    }

    public Object relevanceList(ApiScenarioRequest request, int pageNum, int pageSize) {
        request.setAllowedRepeatCase(testPlanService.isAllowedRepeatCase(request.getPlanId()));
        return microService.postForData(serviceName, BASE_UEL + String.format("/relevance/list/%s/%s", pageNum, pageSize), request);
    }
}
