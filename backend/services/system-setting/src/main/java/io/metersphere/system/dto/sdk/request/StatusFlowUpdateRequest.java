package io.metersphere.system.dto.sdk.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class StatusFlowUpdateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "起始状态ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{status_flow.from_id.not_blank}")
    @Size(min = 1, max = 50, message = "{status_flow.from_id.length_range}")
    private String fromId;

    @Schema(description = "目的状态ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{status_flow.to_id.not_blank}")
    @Size(min = 1, max = 50, message = "{status_flow.to_id.length_range}")
    private String toId;

    @Schema(description = "启用或者禁用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Boolean enable;
}