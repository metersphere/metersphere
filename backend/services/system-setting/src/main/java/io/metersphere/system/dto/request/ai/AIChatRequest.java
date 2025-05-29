package io.metersphere.system.dto.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class AIChatRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "提示词", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String prompt;

    @Schema(description = "模型ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String chatModelId;

    @Schema(description = "对话ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String conversationId;

    @Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String organizationId;
}
