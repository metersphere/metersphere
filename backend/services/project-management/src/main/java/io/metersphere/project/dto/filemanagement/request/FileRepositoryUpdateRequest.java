package io.metersphere.project.dto.filemanagement.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FileRepositoryUpdateRequest {
    @Schema(description = "模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_repository.id.not_blank}")
    private String id;

    @Schema(description = "模块名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 1, max = 255, message = "{file_module.name.length_range}")
    private String name;

    @Schema(description = "存储库类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String platform;

    @Schema(description = "存储库token", requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;

    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userName;
}

