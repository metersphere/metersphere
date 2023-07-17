package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class PluginOrganization implements Serializable {
    @Schema(title = "插件ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{plugin_organization.plugin_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{plugin_organization.plugin_id.length_range}", groups = {Created.class, Updated.class})
    private String pluginId;

    @Schema(title = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{plugin_organization.organization_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{plugin_organization.organization_id.length_range}", groups = {Created.class, Updated.class})
    private String organizationId;

    private static final long serialVersionUID = 1L;
}