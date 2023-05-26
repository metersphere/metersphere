package io.metersphere.api.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class ApiScenarioEnvironment implements Serializable {
    @Schema(title = "场景环境pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_environment.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{api_scenario_environment.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "场景fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_environment.api_scenario_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_scenario_environment.api_scenario_id.length_range}", groups = {Created.class, Updated.class})
    private String apiScenarioId;

    @Schema(title = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_environment.project_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_scenario_environment.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "环境fk")
    private String environmentId;

    @Schema(title = "环境组fk")
    private String environmentGroupId;

    private static final long serialVersionUID = 1L;
}