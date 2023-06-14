package io.metersphere.system.request;

import io.metersphere.sdk.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectRequest extends BasePageRequest {
    @Schema(title = "组织id")
    private String organizationId;
    @Schema(title = "项目ID")
    private String projectId;
}
