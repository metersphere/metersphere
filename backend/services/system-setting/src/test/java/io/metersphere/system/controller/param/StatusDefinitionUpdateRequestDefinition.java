package io.metersphere.system.controller.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class StatusDefinitionUpdateRequestDefinition implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "状态ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{status_definition.status_id.not_blank}")
    @Size(min = 1, max = 50, message = "{status_definition.status_id.length_range}")
    private String statusId;

    @Schema(description = "状态定义ID(在代码中定义)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{status_definition.definition_id.not_blank}")
    @Size(min = 1, max = 100, message = "{status_definition.definition_id.length_range}")
    private String definitionId;

    @Schema(description = "启用或者禁用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Boolean enable;
}