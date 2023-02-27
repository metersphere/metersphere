package io.metersphere.api.dto.automation;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestPlanScenarioDTO extends ApiScenarioDTO {
    private ApiScenarioReportResult response;
}
