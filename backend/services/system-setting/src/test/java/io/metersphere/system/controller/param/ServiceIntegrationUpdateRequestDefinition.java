package io.metersphere.system.controller.param;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

@Data
public class ServiceIntegrationUpdateRequestDefinition {
    @Schema(description =  "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(groups = {Updated.class})
    @Size(min = 1, max = 50, groups = {Updated.class})
    private String id;

    @Schema(description =  "插件的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(groups = {Created.class})
    @Size(min = 1, max = 50, groups = {Created.class, Updated.class})
    private String pluginId;

    @Schema(description =  "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(groups = {Created.class})
    @Size(min = 1, max = 50, groups = {Created.class, Updated.class})
    private String organizationId;

    @Schema(description =  "配置内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(groups = {Created.class})
    private Map<String, String> configuration;
}
