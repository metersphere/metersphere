package io.metersphere.system.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class OperatingLogResource implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{operating_log_resource.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{operating_log_resource.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "Operating log ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{operating_log_resource.operating_log_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{operating_log_resource.operating_log_id.length_range}", groups = {Created.class, Updated.class})
    private String operatingLogId;

    @Schema(title = "operating source id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{operating_log_resource.source_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{operating_log_resource.source_id.length_range}", groups = {Created.class, Updated.class})
    private String sourceId;

    private static final long serialVersionUID = 1L;
}