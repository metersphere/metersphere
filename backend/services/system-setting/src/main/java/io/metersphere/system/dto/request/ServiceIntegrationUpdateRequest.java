
package io.metersphere.system.dto.request;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class ServiceIntegrationUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description =  "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{service_integration.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{service_integration.id.length_range}", groups = {Updated.class})
    private String id;

    @Schema(description =  "插件的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{service_integration.plugin_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{service_integration.plugin_id.length_range}", groups = {Created.class, Updated.class})
    private String pluginId;

    @Schema(description =  "是否启用")
    private Boolean enable;

    @Schema(description =  "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{service_integration.organization_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{service_integration.organization_id.length_range}", groups = {Created.class, Updated.class})
    private String organizationId;

    @Schema(description =  "配置的表单键值对", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{service_integration.configuration.not_blank}", groups = {Created.class})
    private Map<String, Object> configuration;
}