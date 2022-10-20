package io.metersphere.plan.service.remote.api;

import io.metersphere.dto.PlanReportCaseDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PlanApiScenarioReportService extends ApiTestService {

    private static final String BASE_UEL = "/api/scenario/report";

    public List<PlanReportCaseDTO> selectForPlanReport(List<String> apiReportIds) {
        return microService.postForDataArray(serviceName, BASE_UEL + "/plan/report", apiReportIds, PlanReportCaseDTO.class);
    }

    public Map<String, String> getReportStatusByReportIds(List<String> reportIdList) {
        return microService.postForData(serviceName, BASE_UEL + "/plan/status/map", reportIdList, Map.class);
    }

}
