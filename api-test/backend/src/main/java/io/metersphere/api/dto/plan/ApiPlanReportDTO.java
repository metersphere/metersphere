package io.metersphere.api.dto.plan;


import io.metersphere.api.dto.automation.TestPlanFailureApiDTO;
import io.metersphere.api.dto.automation.TestPlanFailureScenarioDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ApiPlanReportDTO {
    private List<TestPlanFailureApiDTO> apiAllCases;
    private List<TestPlanFailureApiDTO> apiFailureCases;
    private List<TestPlanFailureApiDTO> errorReportCases;
    private List<TestPlanFailureApiDTO> unExecuteCases;

    private List<TestPlanFailureScenarioDTO> scenarioAllCases;
    private List<TestPlanFailureScenarioDTO> scenarioFailureCases;
    private List<TestPlanFailureScenarioDTO> errorReportScenarios;
    private List<TestPlanFailureScenarioDTO> unExecuteScenarios;
}
