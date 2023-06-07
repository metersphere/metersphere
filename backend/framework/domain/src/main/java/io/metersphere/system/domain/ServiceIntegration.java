package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class ServiceIntegration implements Serializable {
    @Schema(title = "", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{service_integration.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{service_integration.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "平台", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{service_integration.platform.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{service_integration.platform.length_range}", groups = {Created.class, Updated.class})
    private String platform;

    @Schema(title = "组织ID")
    private String organizationId;

    @Schema(title = "", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{service_integration.configuration.not_blank}", groups = {Created.class})
    private byte[] configuration;

    private static final long serialVersionUID = 1L;
}