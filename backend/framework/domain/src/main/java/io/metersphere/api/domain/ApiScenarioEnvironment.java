package io.metersphere.api.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "场景环境")
@Table("api_scenario_environment")
@Data
public class ApiScenarioEnvironment implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{api_scenario_environment.id.not_blank}", groups = {Updated.class})
    @Schema(title = "场景环境pk", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Size(min = 1, max = 50, message = "{api_scenario_environment.api_scenario_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_environment.api_scenario_id.not_blank}", groups = {Created.class})
    @Schema(title = "场景fk", requiredMode = Schema.RequiredMode.REQUIRED)
    private String apiScenarioId;

    @Size(min = 1, max = 50, message = "{api_scenario_environment.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_environment.project_id.not_blank}", groups = {Created.class})
    @Schema(title = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;

    @Schema(title = "环境fk", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String environmentId;

    @Schema(title = "环境组fk", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String environmentGroupId;

}