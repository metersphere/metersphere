package io.metersphere.project.dto.filemanagement.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RepositoryFileAddRequest {
    @Schema(description = "模块Id")
    @NotBlank(message = "{file_module.id.not_blank}")
    private String moduleId;

    @Schema(description = "分支名")
    @NotBlank(message = "{file_repository.branch.not_blank}")
    private String branch;

    @Schema(description = "文件路径")
    @NotBlank(message = "{file_repository.file_path.not_blank}")
    private String filePath;

    @Schema(description = "开启/关闭(目前用于jar文件)")
    private boolean enable;
}
