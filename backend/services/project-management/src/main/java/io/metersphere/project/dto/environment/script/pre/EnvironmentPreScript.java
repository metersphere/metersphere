package io.metersphere.project.dto.environment.script.pre;


import io.metersphere.project.dto.environment.script.ScriptContent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EnvironmentPreScript {
    @Schema(description = "接口测试")
    private ScriptContent.ApiScript apiPreScript;
    @Schema(description = "UI测试")
    private UiPreScript uiPreScript;

}
