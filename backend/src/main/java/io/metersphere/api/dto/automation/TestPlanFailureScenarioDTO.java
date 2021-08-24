package io.metersphere.api.dto.automation;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestPlanFailureScenarioDTO extends ApiScenarioDTO {
    private APIScenarioReportResult response;
}
