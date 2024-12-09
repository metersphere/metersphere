package io.metersphere.api.dto.scenario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ApiFileCopyRequest {
    @Schema(description = "资源id")
    @NotBlank
    private String resourceId;

    @Schema(description = "文件数组")
    @NotEmpty
    private List<String> fileIds;
}
