package io.metersphere.api.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class ApiEnvironmentConfig implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_environment_config.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{api_environment_config.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "修改时间")
    private Long updateTime;

    @Schema(title = "用户fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_environment_config.create_user.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_environment_config.create_user.length_range}", groups = {Created.class, Updated.class})
    private String createUser;

    @Schema(title = "环境fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_environment_config.environment_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_environment_config.environment_id.length_range}", groups = {Created.class, Updated.class})
    private String environmentId;

    private static final long serialVersionUID = 1L;
}