package io.metersphere.system.request;

import io.metersphere.sdk.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ProjectRequest extends BasePageRequest {
    @Schema(title = "组织id", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String organizationId;
    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String projectId;
}
