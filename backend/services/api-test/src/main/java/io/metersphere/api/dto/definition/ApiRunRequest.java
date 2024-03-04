package io.metersphere.api.dto.definition;

import io.metersphere.api.dto.debug.ApiDebugRunRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
public class ApiRunRequest extends ApiDebugRunRequest {
    @Schema(description = "环境ID")
    private String environmentId;
}
