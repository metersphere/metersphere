package io.metersphere.bug.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class BugFileSourceRequest implements Serializable {

    @Schema(description = "缺陷ID")
    private String bugId;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug.project_id.not_blank}")
    private String projectId;

    @Schema(description = "文件关系ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileId;

    @Schema(description = "是否关联", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean associated;
}
