package io.metersphere.project.dto.environment;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class EnvironmentInfoDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Schema(description = "ID")
    @NotBlank(message = "{project_parameters.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{project_parameters.id.length_range}", groups = {Updated.class})
    private String id;
    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project_application.project_id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{project_parameters.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;
    @Schema(description = "环境名称")
    @NotBlank(message = "{environment_name_is_null}", groups = {Created.class, Updated.class})
    private String name;
    @Schema(description = "环境配置")
    @NotNull(message = "{environment_config_is_null}", groups = {Created.class, Updated.class})
    private EnvironmentConfig config = new EnvironmentConfig();
    @Schema(description = "是否是mock环境")
    private Boolean mock;
    @Schema(description = "描述")
    private String description;

}
