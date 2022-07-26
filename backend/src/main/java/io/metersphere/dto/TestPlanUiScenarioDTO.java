package io.metersphere.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.metersphere.api.dto.automation.APIScenarioReportResult;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestPlanUiScenarioDTO extends UiScenarioDTO {
    private APIScenarioReportResult response;
}
