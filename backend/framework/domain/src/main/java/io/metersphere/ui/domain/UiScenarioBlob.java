package io.metersphere.ui.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class UiScenarioBlob implements Serializable {
    @Schema(title = "场景ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_blob.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{ui_scenario_blob.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "场景定义")
    private byte[] scenarioDefinition;

    @Schema(title = "环境")
    private byte[] environmentJson;

    private static final long serialVersionUID = 1L;
}