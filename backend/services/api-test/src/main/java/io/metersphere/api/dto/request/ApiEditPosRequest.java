package io.metersphere.api.dto.request;

import io.metersphere.system.dto.sdk.request.PosRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class ApiEditPosRequest extends PosRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "模块id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.module_id.not_blank}")
    private String moduleId;

}
