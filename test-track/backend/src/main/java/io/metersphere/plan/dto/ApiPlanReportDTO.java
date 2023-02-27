package io.metersphere.plan.dto;


import io.metersphere.dto.TestPlanApiDTO;
import io.metersphere.dto.TestPlanScenarioDTO;
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
