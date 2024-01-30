package io.metersphere.project.dto.environment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-30  13:57
 */
@Data
public class EnvironmentPluginScriptDTO {
    @Schema(description = "插件ID")
    private String pluginId;
    @Schema(description = "脚本内容")
    private Object script;
}
