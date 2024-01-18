package io.metersphere.api.dto.definition;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ApiReportPageRequest extends BasePageRequest {
    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;

}
