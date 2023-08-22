package io.metersphere.sdk.dto.environment.script.post;


import io.metersphere.sdk.dto.environment.script.ApiScript;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EnvironmentPostScript {
    @Schema(description = "接口测试")
    private ApiScript apiPostScript;
    @Schema(description = "UI测试")
    private UiPostScript uiPostScript;


}
