package io.metersphere.system.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class SystemParameter implements Serializable {
    @Schema(title = "参数名称", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 64]")
    @NotBlank(message = "{system_parameter.param_key.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 64, message = "{system_parameter.param_key.length_range}", groups = {Created.class, Updated.class})
    private String paramKey;

    @Schema(title = "参数值")
    private String paramValue;

    @Schema(title = "类型", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 100]")
    @NotBlank(message = "{system_parameter.type.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 100, message = "{system_parameter.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    private static final long serialVersionUID = 1L;
}