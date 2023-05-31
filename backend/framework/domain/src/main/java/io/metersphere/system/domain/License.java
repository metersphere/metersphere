package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class License implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{license.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{license.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "Create timestamp")
    private Long createTime;

    @Schema(title = "Update timestamp")
    private Long updateTime;

    @Schema(title = "license_code")
    private String licenseCode;

    private static final long serialVersionUID = 1L;
}