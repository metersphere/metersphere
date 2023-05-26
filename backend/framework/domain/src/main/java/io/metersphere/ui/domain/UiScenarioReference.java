package io.metersphere.ui.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class UiScenarioReference implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_reference.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ui_scenario_reference.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "场景ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_reference.ui_scenario_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_scenario_reference.ui_scenario_id.length_range}", groups = {Created.class, Updated.class})
    private String uiScenarioId;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String createUser;

    @Schema(title = "被引用的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_reference.reference_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_scenario_reference.reference_id.length_range}", groups = {Created.class, Updated.class})
    private String referenceId;

    @Schema(title = "引用的数据类型（场景，指令）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_reference.data_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{ui_scenario_reference.data_type.length_range}", groups = {Created.class, Updated.class})
    private String dataType;

    private static final long serialVersionUID = 1L;
}