package io.metersphere.track.dto;

import io.metersphere.api.dto.automation.ApiScenarioDTO;
import io.metersphere.api.dto.definition.TestPlanApiCaseDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FailureTestCasesAdvanceDTO {
    private List<TestPlanCaseDTO> functionalTestCases;
    private List<TestPlanApiCaseDTO> apiTestCases;
    private List<ApiScenarioDTO> scenarioTestCases;
    private List<TestPlanLoadCaseDTO> loadTestCases;
}
