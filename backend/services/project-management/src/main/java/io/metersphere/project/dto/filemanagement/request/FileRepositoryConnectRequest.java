package io.metersphere.project.dto.filemanagement.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class FileRepositoryConnectRequest {
    @Schema(description = "存储库地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{file_repository.url.not_blank}")
    private String url;

    @Schema(description = "存储库token", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{file_repository.token.not_blank}")
    private String token;

    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userName;
}

