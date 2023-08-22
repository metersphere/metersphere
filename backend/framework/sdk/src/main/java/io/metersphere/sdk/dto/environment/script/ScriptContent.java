package io.metersphere.sdk.dto.environment.script;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ScriptContent {

    @Schema(description = "脚本内容")
    private String script;
    @Schema(description = "脚本语言")
    private String scriptLanguage;
    @Schema(description = "是否是jsr223")
    private Boolean jsrEnable;

}
