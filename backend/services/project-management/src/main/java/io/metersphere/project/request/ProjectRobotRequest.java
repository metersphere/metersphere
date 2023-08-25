package io.metersphere.project.request;

import io.metersphere.sdk.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ProjectRobotRequest extends BasePageRequest {
    @Schema(description =  "是否禁用")
    private Boolean enable;
}
