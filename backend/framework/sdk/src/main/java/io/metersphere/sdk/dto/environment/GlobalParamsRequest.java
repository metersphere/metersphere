package io.metersphere.sdk.dto.environment;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class GlobalParamsRequest implements Serializable {

    @Schema(description = "ID")
    @NotBlank(message = "{project_parameters.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{project_parameters.id.length_range}", groups = {Updated.class})
    private String id;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project_parameters.project_id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{project_parameters.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description = "全局参数")
    private GlobalParams globalParams;


    @Serial
    private static final long serialVersionUID = 1L;
}
