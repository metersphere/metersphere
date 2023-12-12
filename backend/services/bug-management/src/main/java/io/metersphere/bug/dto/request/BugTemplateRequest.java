package io.metersphere.bug.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class BugTemplateRequest {

    @Schema(description = "模板ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;
    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;
    @Schema(description = "缺陷当前状态ID")
    private String fromStatusId;
    @Schema(description = "缺陷第三方平台Key")
    private String platformBugKey;
}
