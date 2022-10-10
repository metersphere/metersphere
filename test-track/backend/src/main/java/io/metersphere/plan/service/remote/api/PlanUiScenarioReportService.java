package io.metersphere.plan.service.remote.api;

import io.metersphere.dto.PlanReportCaseDTO;
import io.metersphere.plan.service.remote.ui.UiTestService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PlanUiScenarioReportService extends UiTestService {

    private static final String BASE_UEL = "/ui/scenario/report";

    public List<PlanReportCaseDTO> selectForPlanReport(List<String> apiReportIds) {
       return microService.postForDataArray(serviceName, BASE_UEL + "/plan/report", apiReportIds, PlanReportCaseDTO.class);
    }

    public Map<String, String> getReportStatusByReportIds(List<String> reportIdList) {
        return (Map<String, String>) microService.postForData(serviceName, BASE_UEL + "/plan/status/map", reportIdList);
    }
}
