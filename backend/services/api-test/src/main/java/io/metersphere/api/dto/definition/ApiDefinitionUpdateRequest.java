package io.metersphere.api.dto.definition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author lan
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ApiDefinitionUpdateRequest extends ApiDefinitionAddRequest {

    private static final long serialVersionUID = 1L;

    @Schema(description = "接口pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_definition.id.length_range}")
    private String id;

}
