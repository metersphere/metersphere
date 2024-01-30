package io.metersphere.project.dto.filemanagement.request;

import io.metersphere.sdk.constants.ModuleConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FileModuleCreateRequest {
    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project.id.not_blank}")
    private String projectId;
    
    @Schema(description = "模块名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{file_module.name.not_blank}")
    @Size(min = 1, max = 255, message = "{file_module.name.length_range}")
    private String name;

    @Schema(description = "父模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{parent.node.not_blank}")
    private String parentId = ModuleConstants.ROOT_NODE_PARENT_ID;
}

