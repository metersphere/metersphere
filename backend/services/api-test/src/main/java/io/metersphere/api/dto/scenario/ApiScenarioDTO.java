package io.metersphere.api.dto.scenario;

import io.metersphere.api.domain.ApiScenario;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ApiScenarioDTO extends ApiScenario {
    @Schema(description =  "场景pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_blob.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_scenario_blob.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "场景步骤内容")
    private byte[] content;
}
