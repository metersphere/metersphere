package io.metersphere.api.dto;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ReferenceRequest extends BasePageRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description =  "资源id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{operation_history.source_id.not_blank}")
    private String resourceId;


}
