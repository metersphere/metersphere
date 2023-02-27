package io.metersphere.api.dto.plan;


import io.metersphere.api.dto.automation.TestPlanApiDTO;
import io.metersphere.api.dto.automation.TestPlanScenarioDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ApiPlanReportDTO {
    private List<TestPlanApiDTO> apiAllCases;
    private List<TestPlanApiDTO> apiFailureCases;
    private List<TestPlanApiDTO> errorReportCases;
    private List<TestPlanApiDTO> unExecuteCases;

    private List<TestPlanScenarioDTO> scenarioAllCases;
    private List<TestPlanScenarioDTO> scenarioFailureCases;
    private List<TestPlanScenarioDTO> errorReportScenarios;
    private List<TestPlanScenarioDTO> unExecuteScenarios;
}
