package io.metersphere.plan.service.remote.ui;

import io.metersphere.base.domain.TestPlanUiScenario;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.*;
import io.metersphere.plan.constant.ApiReportStatus;
import io.metersphere.plan.dto.TestCaseReportStatusResultDTO;
import io.metersphere.plan.dto.TestPlanScenarioStepCountSimpleDTO;
import io.metersphere.plan.dto.TestPlanSimpleReportDTO;
import io.metersphere.plan.dto.UiPlanReportDTO;
import io.metersphere.plan.request.api.ApiPlanReportRequest;
import io.metersphere.plan.request.api.ApiScenarioRequest;
import io.metersphere.plan.service.TestPlanService;
import io.metersphere.plan.service.remote.api.PlanTestPlanScenarioCaseService;
import io.metersphere.plan.service.remote.api.PlanUiScenarioReportService;
import io.metersphere.plan.utils.TestPlanStatusCalculator;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class PlanTestPlanUiScenarioCaseService extends UiTestService {

    private static final String BASE_UEL = "/test/plan/uiScenario/case";

    @Resource
    private PlanTestPlanScenarioCaseService planTestPlanScenarioCaseService;

    @Resource
    private PlanUiScenarioReportService planUiScenarioReportService;
    @Resource
    @Lazy
    TestPlanService testPlanService;

    public List<String> getExecResultByPlanId(String planId) {
        return (List<String>) microService.getForData(serviceName, BASE_UEL + "/plan/exec/result/" + planId);
    }

    public UiPlanReportDTO getUiReport(ApiPlanReportRequest request) {
        return microService.postForData(serviceName, BASE_UEL + "/plan/report", request, UiPlanReportDTO.class);
    }

    public void calculatePlanReport(List<String> reportIds, TestPlanSimpleReportDTO report) {
        try {
            List<PlanReportCaseDTO> planReportCaseDTOS = planUiScenarioReportService.selectForPlanReport(reportIds);
            calculatePlanReport(report, planReportCaseDTOS);
        } catch (MSException e) {
            LogUtil.error(e);
        }
    }

    public void calculatePlanReport(TestPlanSimpleReportDTO report, List<PlanReportCaseDTO> planReportCaseDTOS) {
        TestPlanUiResultReportDTO uiResult = getUiResult(report, planReportCaseDTOS);
        report.setUiResult(uiResult);
    }

    public void calculatePlanReport(String planId, TestPlanSimpleReportDTO report) {
        try {
            List<PlanReportCaseDTO> planReportCaseDTOS = selectStatusForPlanReport(planId);

            TestPlanUiResultReportDTO uiResult = getUiResult(report, planReportCaseDTOS);

            report.setUiResult(uiResult);

        } catch (MSException e) {
            LogUtil.error(e);
        }
    }

    @NotNull
    private TestPlanUiResultReportDTO getUiResult(TestPlanSimpleReportDTO report, List<PlanReportCaseDTO> planReportCaseDTOS) {
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
        return microService.postForData(serviceName, BASE_UEL + "/step/count", planReportCaseDTOS, TestPlanScenarioStepCountSimpleDTO.class);
    }

    public List<PlanReportCaseDTO> selectStatusForPlanReport(String planId) {
        return microService.getForDataArray(serviceName, BASE_UEL + "/get/report/status/" + planId, PlanReportCaseDTO.class);
    }

    public void copyPlan(String sourcePlanId, String targetPlanId) {
        microService.getForData(serviceName, BASE_UEL + "/plan/copy/" + sourcePlanId + "/" + targetPlanId);
    }

    public boolean haveUiCase(String planId) {
        try {
            return (boolean) microService.getForData(serviceName, BASE_UEL + "/have/ui/case/" + planId);
        } catch (MSException e) {
            LogUtil.error(e);
            return false;
        }
    }

    public List<TestPlanUiScenario> list(String planId) {
        return microService.getForDataArray(serviceName, BASE_UEL + "/list/" + planId, TestPlanUiScenario.class);
    }

    public Boolean isCaseExecuting(String planId) {
        return (Boolean) microService.getForData(serviceName, BASE_UEL + "/is/executing/" + planId);
    }

    public List<TestPlanUiScenarioDTO> getFailureListByIds(Set<String> ids) {
        return microService.postForDataArray(serviceName, BASE_UEL + "/failure/list", ids, TestPlanUiScenarioDTO.class);
    }

    public List<ModuleNodeDTO> getNodeByPlanId(List<String> projectIds, String planId) {
        return microService.postForDataArray(serviceName, BASE_UEL + "/list/module/" + planId, projectIds, ModuleNodeDTO.class);
    }

    public List<TestPlanUiScenarioDTO> buildResponse(List<TestPlanUiScenarioDTO> uiCases) {
        return microService.postForDataArray(serviceName, BASE_UEL + "/build/response", uiCases, TestPlanUiScenarioDTO.class);
    }

    public Object relevanceList(ApiScenarioRequest request, int pageNum, int pageSize) {
        request.setAllowedRepeatCase(testPlanService.isAllowedRepeatCase(request.getPlanId()));
        return microService.postForData(serviceName, BASE_UEL + String.format("/relevance/list/%s/%s", pageNum, pageSize), request);
    }
}
