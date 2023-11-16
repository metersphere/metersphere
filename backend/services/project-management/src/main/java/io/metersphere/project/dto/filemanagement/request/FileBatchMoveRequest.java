package io.metersphere.project.dto.filemanagement.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class FileBatchMoveRequest extends FileBatchProcessRequest {
    @Schema(description = "目标模块ID")
    @NotEmpty(message = "{file_module_blob.file_module_id.not_blank}")
    private String moveModuleId;
}
