package io.metersphere.ui.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class UiElementScenarioReference implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element_scenario_reference.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{ui_element_scenario_reference.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "元素ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element_scenario_reference.element_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_element_scenario_reference.element_id.length_range}", groups = {Created.class, Updated.class})
    private String elementId;

    @Schema(title = "元素模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element_scenario_reference.element_module_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_element_scenario_reference.element_module_id.length_range}", groups = {Created.class, Updated.class})
    private String elementModuleId;

    @Schema(title = "场景ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element_scenario_reference.scenario_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_element_scenario_reference.scenario_id.length_range}", groups = {Created.class, Updated.class})
    private String scenarioId;

    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element_scenario_reference.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_element_scenario_reference.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    private static final long serialVersionUID = 1L;
}