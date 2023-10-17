package io.metersphere.sdk.dto.request;

import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.valid.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class StatusDefinitionUpdateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "组织ID或项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(min = 1, max = 50)
    private String scopeId;

    @Schema(description = "使用场景", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{status_item.scene.not_blank}")
    @EnumValue(enumClass = TemplateScene.class)
    private String scene;

    @NotBlank
    @Valid
    private List<StatusDefinitionRequest> statusDefinitions;

    @Data
    public static class StatusDefinitionRequest implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "状态ID", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{status_definition.status_id.not_blank}")
        @Size(min = 1, max = 50, message = "{status_definition.status_id.length_range}")
        private String statusId;

        @Schema(description = "状态定义ID(在代码中定义)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{status_definition.definition_id.not_blank}")
        @Size(min = 1, max = 100, message = "{status_definition.definition_id.length_range}")
        private String definitionId;
    }
}