package io.metersphere.system.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class ServiceIntegration implements Serializable {
    @Schema(title = "", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    @NotBlank(message = "{service_integration.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{service_integration.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "平台", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    @NotBlank(message = "{service_integration.platform.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{service_integration.platform.length_range}", groups = {Created.class, Updated.class})
    private String platform;

    @Schema(title = "工作空间ID")
    private String workspaceId;

    @Schema(title = "", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 65535]")
    @NotBlank(message = "{service_integration.configuration.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 65535, message = "{service_integration.configuration.length_range}", groups = {Created.class, Updated.class})
    private byte[] configuration;

    private static final long serialVersionUID = 1L;
}