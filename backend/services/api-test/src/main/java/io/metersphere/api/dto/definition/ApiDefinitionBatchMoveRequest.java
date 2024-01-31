package io.metersphere.api.dto.definition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * @author lan
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ApiDefinitionBatchMoveRequest extends ApiDefinitionBatchRequest {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 50, message = "{api_definition.module_id.length_range}")
    private String moduleId;

}
