package io.metersphere.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.metersphere.base.domain.UiScenarioWithBLOBs;
import io.metersphere.plan.dto.APIScenarioReportResult;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)  // todo check
public class TestPlanUiScenarioDTO extends UiScenarioWithBLOBs {
    private APIScenarioReportResult response;
}
