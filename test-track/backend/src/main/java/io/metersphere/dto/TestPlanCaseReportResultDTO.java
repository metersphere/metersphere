package io.metersphere.dto;

import io.metersphere.plan.dto.ApiPlanReportDTO;
import io.metersphere.plan.dto.UiPlanReportDTO;
import io.metersphere.plan.request.performance.LoadPlanReportDTO;
import io.metersphere.xpack.track.dto.IssuesDao;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TestPlanCaseReportResultDTO {
    private Map<String, String> testPlanApiCaseIdAndReportIdMap;
    private Map<String, String> testPlanScenarioIdAndReportIdMap;
    private Map<String, String> testPlanUiScenarioIdAndReportIdMap;
    private Map<String, String> testPlanLoadCaseIdAndReportIdMap;
    private ApiPlanReportDTO apiPlanReportDTO;
    private LoadPlanReportDTO loadPlanReportDTO;
    private UiPlanReportDTO uiPlanReportDTO;
    private List<TestPlanCaseDTO> functionCaseList;
    private List<IssuesDao> issueList;
}
