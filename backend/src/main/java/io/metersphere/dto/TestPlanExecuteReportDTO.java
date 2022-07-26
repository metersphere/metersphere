package io.metersphere.dto;

import io.metersphere.api.dto.automation.TestPlanFailureApiDTO;
import io.metersphere.api.dto.automation.TestPlanFailureScenarioDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class TestPlanExecuteReportDTO {
    private Map<String,String> testPlanApiCaseIdAndReportIdMap;
    private Map<String,String> testPlanScenarioIdAndReportIdMap;
    private Map<String,String> testPlanUiScenarioIdAndReportIdMap;
    private Map<String,String> testPlanLoadCaseIdAndReportIdMap;
    private Map<String,TestPlanFailureApiDTO> apiCaseInfoDTOMap;
    private Map<String,TestPlanFailureScenarioDTO> scenarioInfoDTOMap;
    private Map<String,TestPlanUiScenarioDTO> uiScenarioInfoDTOMap;
}
