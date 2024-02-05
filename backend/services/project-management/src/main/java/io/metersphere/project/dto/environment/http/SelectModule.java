package io.metersphere.project.dto.environment.http;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SelectModule {
    @Schema(description = "模块ID")
    private String moduleId;
    @Schema(description = "是否包含新增子模块")
    private Boolean containChildModule = false;
}
