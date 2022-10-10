package io.metersphere.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.metersphere.plan.dto.APIScenarioReportResult;
import io.metersphere.plan.dto.ApiScenarioDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestPlanFailureScenarioDTO extends ApiScenarioDTO {
    private APIScenarioReportResult response;
}
