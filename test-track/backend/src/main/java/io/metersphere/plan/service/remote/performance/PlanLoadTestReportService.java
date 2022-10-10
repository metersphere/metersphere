package io.metersphere.plan.service.remote.performance;

import io.metersphere.commons.constants.PerformanceTestStatus;
import io.metersphere.commons.constants.TestPlanLoadCaseStatus;
import io.metersphere.dto.PlanReportCaseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PlanLoadTestReportService extends LoadTestService {

    private static final String BASE_UEL = "/performance/report";

    public List<PlanReportCaseDTO> getPlanReportCaseDTO(List<String> reportIds) {
        List<String> statusList = selectForPlanReport(reportIds);
        // 性能测试的报告状态跟用例的执行状态不一样
        for (int i = 0; i < statusList.size(); i++) {
            String item = statusList.get(i);
            if (item.equals(PerformanceTestStatus.Completed.name())) {
                statusList.set(i, TestPlanLoadCaseStatus.success.name());
            } else if (item.equals(PerformanceTestStatus.Error.name())) {
                statusList.set(i, TestPlanLoadCaseStatus.error.name());
            } else {
                statusList.set(i, TestPlanLoadCaseStatus.run.name());
            }
        }
        List<PlanReportCaseDTO> planReportCaseDTOs = new ArrayList<>();
        statusList.forEach(item -> {
            PlanReportCaseDTO planReportCaseDTO = new PlanReportCaseDTO();
            planReportCaseDTO.setStatus(item);
            planReportCaseDTOs.add(planReportCaseDTO);
        });
        return planReportCaseDTOs;
    }

    public List<String> selectForPlanReport(List<String> apiReportIds) {
       return (List<String>) microService.postForData(serviceName, BASE_UEL + "/plan/report", apiReportIds);
    }

    public Map<String, String> selectReportResultByReportIds(List<String> reportIds) {
        return (Map<String, String>) microService.postForData(serviceName, BASE_UEL + "/plan/status/map", reportIds);
    }
}
