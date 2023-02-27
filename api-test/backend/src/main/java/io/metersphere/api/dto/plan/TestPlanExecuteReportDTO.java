package io.metersphere.api.dto.plan;

import io.metersphere.api.dto.automation.TestPlanApiDTO;
import io.metersphere.api.dto.automation.TestPlanScenarioDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TestPlanExecuteReportDTO {
    private Map<String, String> testPlanApiCaseIdAndReportIdMap;
    private Map<String, String> testPlanScenarioIdAndReportIdMap;
    private Map<String, TestPlanApiDTO> apiCaseInfoDTOMap;
    private Map<String, TestPlanScenarioDTO> scenarioInfoDTOMap;
}
