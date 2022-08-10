package io.metersphere.api.dto.automation;

import io.metersphere.dto.RunModeConfigDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApiScenarioReportInitDTO {
    private String id;
    private String scenarioId;
    private String scenarioName;
    private String triggerMode;
    private String execType;
    private String projectId;
    private String userId;
    private RunModeConfigDTO config;
    private String relevanceTestPlanReportId;
}
