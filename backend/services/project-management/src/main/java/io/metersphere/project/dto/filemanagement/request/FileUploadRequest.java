package io.metersphere.project.dto.filemanagement.request;

import io.metersphere.sdk.constants.ModuleConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FileUploadRequest {
    @Schema(description = "项目Id")
    @NotBlank(message = "{project.id.not_blank}")
    private String projectId;

    @Schema(description = "模块Id")
    @NotBlank(message = "{file_module.id.not_blank}")
    private String moduleId = ModuleConstants.DEFAULT_NODE_ID;

    @Schema(description = "是否启用")
    private boolean enable;
}
