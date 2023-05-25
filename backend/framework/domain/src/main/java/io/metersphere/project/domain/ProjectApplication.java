package io.metersphere.project.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class ProjectApplication implements Serializable {
    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    @NotBlank(message = "{project_application.project_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{project_application.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "配置项", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    @NotBlank(message = "{project_application.type.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{project_application.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(title = "配置值")
    private String typeValue;

    private static final long serialVersionUID = 1L;
}