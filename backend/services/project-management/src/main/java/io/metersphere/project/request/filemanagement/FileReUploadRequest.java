package io.metersphere.project.request.filemanagement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FileReUploadRequest {
    @Schema(description = "文件Id")
    @NotBlank(message = "{file_metadata.id.not_blank}")
    private String fileId;
}
