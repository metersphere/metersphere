package io.metersphere.plan.service.remote.ui;

import io.metersphere.base.domain.TestPlanReport;
import io.metersphere.base.domain.TestPlanUiScenario;
import io.metersphere.base.domain.UiScenarioReportWithBLOBs;
import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.*;
import io.metersphere.plan.constant.ApiReportStatus;
import io.metersphere.plan.dto.*;
import io.metersphere.plan.request.api.ApiScenarioRequest;
import io.metersphere.plan.request.ui.UiPlanReportRequest;
import io.metersphere.plan.service.TestPlanService;
import io.metersphere.plan.service.remote.api.PlanTestPlanScenarioCaseService;
import io.metersphere.plan.service.remote.api.PlanUiScenarioReportService;
import io.metersphere.plan.utils.TestPlanReportUtil;
import io.metersphere.plan.utils.TestPlanStatusCalculator;
import io.metersphere.request.ResetOrderRequest;
import io.metersphere.utils.BatchProcessingUtil;
import io.metersphere.utils.DiscoveryUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlanTestPlanUiScenarioCaseService extends UiTestService {

    private static final String BASE_URL = "/test/plan/uiScenario/case";

    @Resource
    private PlanTestPlanScenarioCaseService planTestPlanScenarioCaseService;

    @Resource
    private PlanUiScenarioReportService planUiScenarioReportService;
    @Resource
    @Lazy
    TestPlanService testPlanService;

    public List<String> getExecResultByPlanId(String planId) {
        return (List<String>) microService.getForData(serviceName, BASE_URL + "/plan/exec/result/" + planId);
    }

    public UiPlanReportDTO getUiReport(UiPlanReportRequest request) {
        return microService.postForData(serviceName, BASE_URL + "/plan/report", request, UiPlanReportDTO.class);
    }

    public void calculateReportByUiScenarios(List<TestPlanUiScenarioDTO> uiScenarioDTOList, TestPlanReportDataStruct report) {
        try {
            List<PlanReportCaseDTO> planReportCaseDTOList = new ArrayList<>();
            uiScenarioDTOList.forEach(item -> {
                PlanReportCaseDTO dto = new PlanReportCaseDTO();
                dto.setId(item.getId());
                dto.setStatus(item.getStatus());
                dto.setReportId(item.getReportId());
                dto.setCaseId(item.getId());
                planReportCaseDTOList.add(dto);
            });
            calculatePlanReport(report, planReportCaseDTOList);
        } catch (MSException e) {
            LogUtil.error(e);
        }
    }

    public void calculatePlanReport(List<String> reportIds, TestPlanReportDataStruct report) {
        try {
            List<PlanReportCaseDTO> planReportCaseDTOS = planUiScenarioReportService.selectForPlanReport(reportIds);
            calculatePlanReport(report, planReportCaseDTOS);
        } catch (MSException e) {
            LogUtil.error(e);
        }
    }

    public void calculatePlanReport(TestPlanReportDataStruct report, List<PlanReportCaseDTO> planReportCaseDTOS) {
        TestPlanUiResultReportDTO uiResult = getUiResult(report, planReportCaseDTOS);
        report.setUiResult(uiResult);
    }

    public void calculatePlanReport(String planId, TestPlanReportDataStruct report) {
        if (DiscoveryUtil.hasService(MicroServiceName.UI_TEST)) {
            List<PlanReportCaseDTO> planReportCaseDTOS = selectStatusForPlanReport(planId);

            TestPlanUiResultReportDTO uiResult = getUiResult(report, planReportCaseDTOS);
            //记录UI用例的运行环境信息
            List<String> idList = planReportCaseDTOS.stream().map(PlanReportCaseDTO::getId).collect(Collectors.toList());
            try {
                Map<String, List<String>> projectEnvMap = getPlanProjectEnvMap(idList);
                report.setProjectEnvMap(TestPlanReportUtil.mergeProjectEnvMap(projectEnvMap, report.getProjectEnvMap()));
            } catch (Exception e) {
                LogUtil.error(e);
            }
            report.setUiResult(uiResult);
        }
    }

    @NotNull
    private TestPlanUiResultReportDTO getUiResult(TestPlanReportDataStruct report, List<PlanReportCaseDTO> planReportCaseDTOS) {
        TestPlanUiResultReportDTO uiResult = report.getUiResult();

        List<TestCaseReportStatusResultDTO> statusResult = new ArrayList<>();
        Map<String, TestCaseReportStatusResultDTO> statusResultMap = new HashMap<>();
        TestPlanStatusCalculator.buildStatusResultMap(planReportCaseDTOS, statusResultMap, report, ApiReportStatus.SUCCESS.name());
        TestPlanStatusCalculator.addToReportCommonStatusResultList(statusResultMap, statusResult);

        TestPlanScenarioStepCountSimpleDTO stepCountResult = getStepCount(planReportCaseDTOS);
        TestPlanScenarioStepCountDTO stepCount = stepCountResult.getStepCount();
        int underwayStepsCounts = stepCountResult.getUnderwayStepsCounts();

        List<TestCaseReportStatusResultDTO> stepResult = planTestPlanScenarioCaseService.getStepResult(stepCount, underwayStepsCounts);
        uiResult.setUiScenarioData(statusResult);
        uiResult.setUiScenarioStepData(stepResult);
        return uiResult;
    }


    private TestPlanScenarioStepCountSimpleDTO getStepCount(List<PlanReportCaseDTO> planReportCaseDTOS) {
        return microService.postForData(serviceName, BASE_URL + "/step/count", planReportCaseDTOS, TestPlanScenarioStepCountSimpleDTO.class);
    }

    public List<PlanReportCaseDTO> selectStatusForPlanReport(String planId) {
        return microService.getForDataArray(serviceName, BASE_URL + "/get/report/status/" + planId, PlanReportCaseDTO.class);
    }

    public void copyPlan(String sourcePlanId, String targetPlanId) {
        microService.getForData(serviceName, BASE_URL + "/plan/copy/" + sourcePlanId + "/" + targetPlanId);
    }

    public boolean haveUiCase(String planId) {
        try {
            return (boolean) microService.getForData(serviceName, BASE_URL + "/have/ui/case/" + planId);
        } catch (MSException e) {
            LogUtil.error(e);
            return false;
        }
    }

    public List<TestPlanUiScenario> list(String planId) {
        return microService.getForDataArray(serviceName, BASE_URL + "/list/" + planId, TestPlanUiScenario.class);
    }

    public Boolean isCaseExecuting(String planId) {
        return (Boolean) microService.getForData(serviceName, BASE_URL + "/is/executing/" + planId);
    }

    public List<TestPlanUiScenarioDTO> getFailureListByIds(Set<String> ids) {
        return microService.postForDataArray(serviceName, BASE_URL + "/failure/list", ids, TestPlanUiScenarioDTO.class);
    }

    public List<ModuleNodeDTO> getNodeByPlanId(List<String> projectIds, String planId) {
        return microService.postForDataArray(serviceName, BASE_URL + "/list/module/" + planId, projectIds, ModuleNodeDTO.class);
    }

    public List<TestPlanUiScenarioDTO> buildResponse(List<TestPlanUiScenarioDTO> uiCases) {
        if (CollectionUtils.isEmpty(uiCases)) {
            return null;
        }

        //分批处理参数时为了不影响初始参数，这里使用新的对象进行处理
        List<TestPlanUiScenarioDTO> paramList = new ArrayList<>(uiCases);
        List<TestPlanUiScenarioDTO> returnList = new ArrayList<>();
        while (CollectionUtils.isNotEmpty(paramList)) {
            List<TestPlanUiScenarioDTO> requestList = BatchProcessingUtil.subList(paramList, 5);
            returnList.addAll(microService.postForDataArray(serviceName, BASE_URL + "/build/response", uiCases, TestPlanUiScenarioDTO.class));
            paramList.removeAll(requestList);
        }
        return returnList;
    }

    public Object relevanceList(ApiScenarioRequest request, int pageNum, int pageSize) {
        request.setAllowedRepeatCase(testPlanService.isAllowedRepeatCase(request.getPlanId()));
        return microService.postForData(serviceName, BASE_URL + String.format("/relevance/list/%s/%s", pageNum, pageSize), request);
    }

    public void orderCase(ResetOrderRequest request) {
        microService.postForData(serviceName, BASE_URL + "/edit/order", request);
    }

    public void relevanceByTestIds(List<String> uiScenarioIds, String planId) {
        microService.postForData(serviceName, BASE_URL + "/relevance/" + planId, uiScenarioIds);
    }

    public List<UiScenarioReportWithBLOBs> selectExtForPlanReport(String planId) {
        return microService.getForDataArray(serviceName, BASE_URL + "/get/report/ext/" + planId, UiScenarioReportWithBLOBs.class);
    }

    public Map<String, List<String>> getUiScenarioEnv(String planId) {
        return microService.getForData(serviceName, BASE_URL + "/get/env/" + planId, Map.class);
    }

    public List<String> getUiScenarioProjectIds(String planId) {
        return microService.getForData(serviceName, BASE_URL + "/get/project/ids/" + planId, List.class);
    }

    public RunModeConfigDTO setScenarioEnv(String planId, RunModeConfigDTO runModeConfig) {
        return microService.postForData(serviceName, BASE_URL + "/set/env/" + planId, runModeConfig, RunModeConfigDTO.class);
    }

    public TestPlanEnvInfoDTO generateEnvironmentInfo(TestPlanReport testPlanReport) {
        return microService.postForData(serviceName, BASE_URL + "/env/generate", testPlanReport, TestPlanEnvInfoDTO.class);
    }

    public Map<String, List<String>> getPlanProjectEnvMap(List<String> resourceIds) {
        return (Map<String, List<String>>) microService.postForData(serviceName, BASE_URL + "/get/plan/env/map", resourceIds);
    }
}
