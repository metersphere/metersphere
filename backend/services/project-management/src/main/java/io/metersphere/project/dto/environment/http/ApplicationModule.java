package io.metersphere.project.dto.environment.http;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ApplicationModule {
    @Schema(description = "接口测试")
    private Boolean apiTest = true;
    @Schema(description = "UI测试")
    private Boolean uiTest = false;
}
