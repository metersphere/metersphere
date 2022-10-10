package io.metersphere.plan.service.remote.api;

import io.metersphere.dto.PlanReportCaseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class PlanApiDefinitionExecResultService extends ApiTestService {

    private static final String BASE_UEL = "/api/definition/exec/result";

    public List<PlanReportCaseDTO> selectForPlanReport(List<String> apiReportIds) {
       return microService.postForDataArray(serviceName, BASE_UEL + "/plan/report", apiReportIds, PlanReportCaseDTO.class);
    }

    public Map<String, String> selectReportResultByReportIds(List<String> reportIds) {
        return (Map<String, String>) microService.postForData(serviceName, BASE_UEL + "/plan/status/map", reportIds);
    }
}
