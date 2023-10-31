package io.metersphere.project.request.filemanagement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class FileBatchMoveRequest extends FileBatchProcessRequest {
    @Schema(description = "目标模块ID")
    @NotEmpty(message = "{file_module_blob.file_module_id.not_blank}")
    private String moveModuleId;
}
