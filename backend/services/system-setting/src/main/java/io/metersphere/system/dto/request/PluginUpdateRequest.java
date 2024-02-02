package io.metersphere.system.dto.request;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class PluginUpdateRequest {
    @Schema(description =  "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{plugin.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{plugin.id.length_range}", groups = {Updated.class})
    private String id;

    @Schema(description =  "插件名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{plugin.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{plugin.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description =  "是否启用插件, 默认启用")
    private Boolean enable;

    @Schema(description =  "是否是全局插件, 默认全局")
    private Boolean global;

    @Schema(description =  "插件描述")
    @Size(max = 1000, message = "{plugin.scenario.length_range}", groups = {Created.class, Updated.class})
    private String description;

    @Schema(hidden = true)
    private String createUser;

    @Schema(description =  "关联的组织ID")
    private List<String> organizationIds;
}
