package io.metersphere.system.controller.param;

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
public class StatusFlowUpdateRequestDefinition implements Serializable {
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
    private List<StatusFlowRequestDefinition> statusFlows;

    @Data
    public static class StatusFlowRequestDefinition implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "起始状态ID", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{status_flow.from_id.not_blank}")
        @Size(min = 1, max = 50, message = "{status_flow.from_id.length_range}")
        private String fromId;

        @Schema(description = "目的状态ID", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{status_flow.to_id.not_blank}")
        @Size(min = 1, max = 50, message = "{status_flow.to_id.length_range}")
        private String toId;
    }
}