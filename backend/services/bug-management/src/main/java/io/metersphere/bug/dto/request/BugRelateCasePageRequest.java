package io.metersphere.bug.dto.request;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class BugRelateCasePageRequest extends BasePageRequest {

    @Schema(description = "所属项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;

    @Schema(description = "版本ID")
    private String versionId;

    @Schema(description = "选中模块ID")
    private String selectModuleId;
}
