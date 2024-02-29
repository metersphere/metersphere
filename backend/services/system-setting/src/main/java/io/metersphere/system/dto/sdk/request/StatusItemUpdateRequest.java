package io.metersphere.system.dto.sdk.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class StatusItemUpdateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "状态ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{status_item.id.not_blank}")
    @Size(min = 1, max = 50, message = "{status_item.id.length_range}")
    private String id;

    @Schema(description = "状态名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 1, max = 255, message = "{status_item.name.length_range}")
    private String name;

    @Schema(description = "状态说明")
    @Size(max = 1000)
    private String remark;
}