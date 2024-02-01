package io.metersphere.api.dto.scenario;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ApiScenarioAssociationPageRequest extends BasePageRequest {

    @Schema(description = "场景pk")
    @Size(min = 1, max = 50, message = "{api_scenario_step.scenario_id.length_range}")
    private String id;
}
