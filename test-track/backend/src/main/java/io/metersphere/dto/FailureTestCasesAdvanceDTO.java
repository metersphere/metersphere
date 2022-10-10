package io.metersphere.dto;

import io.metersphere.plan.dto.ApiScenarioDTO;
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
