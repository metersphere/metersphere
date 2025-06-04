package io.metersphere.system.dto.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class AIConversationUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String id;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;
}
