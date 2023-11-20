package io.metersphere.api.dto.definition;

import io.swagger.v3.oas.annotations.media.Schema;
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
    private String moduleId;

}
