package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class SystemParameter implements Serializable {
    @Schema(title = "参数名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{system_parameter.param_key.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{system_parameter.param_key.length_range}", groups = {Created.class, Updated.class})
    private String paramKey;

    @Schema(title = "参数值")
    private String paramValue;

    @Schema(title = "类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{system_parameter.type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 100, message = "{system_parameter.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    private static final long serialVersionUID = 1L;
}