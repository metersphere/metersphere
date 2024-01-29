package io.metersphere.bug.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class BugSyncRequest implements Serializable {

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;

    @Schema(description = "创建时间前或后", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean pre;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long createTime;
}
