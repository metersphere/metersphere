package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class Plugin implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{plugin.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{plugin.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "plugin name")
    private String name;

    @Schema(title = "Plugin id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{plugin.plugin_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 300, message = "{plugin.plugin_id.length_range}", groups = {Created.class, Updated.class})
    private String pluginId;

    @Schema(title = "Ui script id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{plugin.script_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 300, message = "{plugin.script_id.length_range}", groups = {Created.class, Updated.class})
    private String scriptId;

    @Schema(title = "Plugin clazzName", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{plugin.clazz_name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 500, message = "{plugin.clazz_name.length_range}", groups = {Created.class, Updated.class})
    private String clazzName;

    @Schema(title = "Jmeter base clazzName", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{plugin.jmeter_clazz.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 300, message = "{plugin.jmeter_clazz.length_range}", groups = {Created.class, Updated.class})
    private String jmeterClazz;

    @Schema(title = "Plugin jar path", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{plugin.source_path.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 300, message = "{plugin.source_path.length_range}", groups = {Created.class, Updated.class})
    private String sourcePath;

    @Schema(title = "Plugin jar name", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{plugin.source_name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 300, message = "{plugin.source_name.length_range}", groups = {Created.class, Updated.class})
    private String sourceName;

    @Schema(title = "plugin init entry class")
    private String execEntry;

    @Schema(title = "")
    private Long createTime;

    @Schema(title = "")
    private Long updateTime;

    @Schema(title = "")
    private String createUser;

    @Schema(title = "Is xpack plugin")
    private Boolean xpack;

    @Schema(title = "Plugin usage scenarios", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{plugin.scenario.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{plugin.scenario.length_range}", groups = {Created.class, Updated.class})
    private String scenario;

    private static final long serialVersionUID = 1L;
}