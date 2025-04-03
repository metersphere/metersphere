package io.metersphere.system.dto.request.ai;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ModelSourceRequest extends BasePageRequest {

    @Schema(description =  "组织id/个人id")
    @NotBlank(message = "{model_source.owner.not_blank}")
    private String owner;

}
