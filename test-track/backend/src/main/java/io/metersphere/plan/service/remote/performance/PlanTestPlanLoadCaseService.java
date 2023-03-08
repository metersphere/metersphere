package io.metersphere.plan.service.remote.performance;

import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.commons.constants.TestPlanLoadCaseStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.PlanReportCaseDTO;
import io.metersphere.dto.TestPlanLoadCaseDTO;
import io.metersphere.dto.TestPlanLoadResultReportDTO;
import io.metersphere.plan.dto.TestCaseReportStatusResultDTO;
import io.metersphere.plan.dto.TestPlanSimpleReportDTO;
import io.metersphere.plan.request.api.ApiPlanReportRequest;
import io.metersphere.plan.request.performance.LoadCaseRequest;
import io.metersphere.plan.request.performance.LoadPlanReportDTO;
import io.metersphere.plan.service.TestPlanService;
import io.metersphere.plan.utils.TestPlanStatusCalculator;
import io.metersphere.utils.DiscoveryUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlanTestPlanLoadCaseService extends LoadTestService {

    private static final String BASE_UEL = "/test/plan/load/case";

    @Resource
    private PlanLoadTestReportService planLoadTestReportService;
    @Resource
    @Lazy
    private TestPlanService testPlanService;

    public void calculatePlanReport(String planId, TestPlanSimpleReportDTO report) {
        if (DiscoveryUtil.hasService(MicroServiceName.PERFORMANCE_TEST)) {
            List<PlanReportCaseDTO> planReportCaseDTOS = selectStatusForPlanReport(planId);
            calculatePlanReport(report, planReportCaseDTOS);
        }
    }

    public void calculateReportByLoadCaseList(List<TestPlanLoadCaseDTO> testPlanLoadCaseDTOList, TestPlanSimpleReportDTO report) {
        try {
            List<PlanReportCaseDTO> planReportCaseDTOList = new ArrayList<>();
            testPlanLoadCaseDTOList.forEach(item -> {
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

    public void calculatePlanReport(List<String> reportIds, TestPlanSimpleReportDTO report) {
        try {
            List<PlanReportCaseDTO> planReportCaseDTOs = planLoadTestReportService.getPlanReportCaseDTO(reportIds);
            calculatePlanReport(report, planReportCaseDTOs);
        } catch (MSException e) {
            LogUtil.error(e);
        }
    }

    private void calculatePlanReport(TestPlanSimpleReportDTO report, List<PlanReportCaseDTO> planReportCaseDTOS) {
        TestPlanLoadResultReportDTO loadResult = new TestPlanLoadResultReportDTO();
        report.setLoadResult(loadResult);
        List<TestCaseReportStatusResultDTO> statusResult = new ArrayList<>();
        Map<String, TestCaseReportStatusResultDTO> statusResultMap = new HashMap<>();

        TestPlanStatusCalculator.buildStatusResultMap(planReportCaseDTOS, statusResultMap, report, TestPlanLoadCaseStatus.success.name());
        TestPlanStatusCalculator.addToReportCommonStatusResultList(statusResultMap, statusResult);

        loadResult.setCaseData(statusResult);
    }


    public List<String> getStatusByTestPlanId(String planId) {
        return (List<String>) microService.getForData(serviceName, BASE_UEL + "/plan/status/" + planId);
    }

    public List<TestPlanLoadCaseDTO> list(String planId) {
        return microService.getForDataArray(serviceName, BASE_UEL + "/list/" + planId, TestPlanLoadCaseDTO.class);
    }

    public List<TestPlanLoadCaseDTO> list(LoadCaseRequest request) {
        return microService.postForDataArray(serviceName, BASE_UEL + "/list", request, TestPlanLoadCaseDTO.class);
    }

    public void copyPlan(String sourcePlanId, String targetPlanId) {
        microService.getForData(serviceName, BASE_UEL + "/plan/copy/" + sourcePlanId + "/" + targetPlanId);
    }

    public LoadPlanReportDTO getLoadReport(ApiPlanReportRequest request) {
        return microService.postForData(serviceName, BASE_UEL + "/plan/report", request, LoadPlanReportDTO.class);
    }

    public LoadPlanReportDTO getLoadExecuteReport(ApiPlanReportRequest request) {
        return microService.postForData(serviceName, BASE_UEL + "/plan/execute/report", request, LoadPlanReportDTO.class);
    }

    public Boolean haveExecCase(String planId) {
        try {
            return (Boolean) microService.getForData(serviceName, BASE_UEL + "/have/exec/" + planId);
        } catch (MSException e) {
            LogUtil.error(e);
            return false;
        }
    }

    public void relevanceByTestIds(List<String> performanceIds, String planId) {
        microService.postForData(serviceName, BASE_UEL + "/relevance/" + planId, performanceIds);
    }

    public List<String> getExecResultByPlanId(String planId) {
        return (List<String>) microService.getForData(serviceName, BASE_UEL + "/plan/exec/result/" + planId);
    }

    public List<PlanReportCaseDTO> selectStatusForPlanReport(String planId) {
        return microService.getForDataArray(serviceName, BASE_UEL + "/get/report/status/" + planId, PlanReportCaseDTO.class);
    }

    public Boolean hasFailCase(String planId, List<String> performanceIds) {
        return (Boolean) microService.postForData(serviceName, BASE_UEL + "/has/fail/" + planId, performanceIds);
    }

    public Boolean isCaseExecuting(String planId, String projectId) {
        return (Boolean) microService.getForData(serviceName, BASE_UEL + "/is/executing/" + planId + "/" + projectId);
    }

    public List<TestPlanLoadCaseDTO> buildResponse(List<TestPlanLoadCaseDTO> loadCases) {
        if (CollectionUtils.isEmpty(loadCases)) {
            return null;
        }
        return microService.postForDataArray(serviceName, BASE_UEL + "/build/response", loadCases, TestPlanLoadCaseDTO.class);
    }

    public Object relevanceList(int pageNum, int pageSize, LoadCaseRequest request) {
        request.setAllowedRepeatCase(testPlanService.isAllowedRepeatCase(request.getTestPlanId()));
        return microService.postForData(serviceName, BASE_UEL + String.format("/relevance/list/%s/%s", pageNum, pageSize), request);
    }

    public String getPlanLoadCaseResourcePoolId(String loadReportId) {
        return (String) microService.getForData(serviceName, BASE_UEL + "/pool/" + loadReportId);
    }

}
