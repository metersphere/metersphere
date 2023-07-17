package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class PluginFrontScript implements Serializable {
    @Schema(title = "插件的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{plugin_front_script.plugin_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{plugin_front_script.plugin_id.length_range}", groups = {Created.class, Updated.class})
    private String pluginId;

    @Schema(title = "插件中对应表单配置的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{plugin_front_script.script_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{plugin_front_script.script_id.length_range}", groups = {Created.class, Updated.class})
    private String scriptId;

    @Schema(title = "脚本内容")
    private String script;

    private static final long serialVersionUID = 1L;
}