package io.metersphere.functional.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author wx
 */
@Data
public class FunctionalCaseBatchMoveRequest extends FunctionalCaseBatchRequest {

    @Schema(description = "模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String moduleId;
}
