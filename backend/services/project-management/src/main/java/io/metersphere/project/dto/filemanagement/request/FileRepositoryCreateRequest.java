package io.metersphere.project.dto.filemanagement.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FileRepositoryCreateRequest {
    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project.id.not_blank}")
    private String projectId;

    @Schema(description = "模块名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_repository.name.not_blank}")
    @Size(min = 1, max = 255, message = "{file_module.name.length_range}")
    private String name;

    @Schema(description = "存储库类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_repository.type.not_blank}")
    private String platform;

    @Schema(description = "存储库地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_repository.url.not_blank}")
    @Size(min = 1, max = 255, message = "Url " + "{length.too.large}")
    private String url;

    @Schema(description = "存储库token", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{file_repository.token.not_blank}")
    private String token;

    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userName;
}

