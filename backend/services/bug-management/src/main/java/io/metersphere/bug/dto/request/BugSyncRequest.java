package io.metersphere.bug.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class BugSyncRequest {

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;

    @Schema(description = "创建时间前或后", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean pre;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long createTime;
}
