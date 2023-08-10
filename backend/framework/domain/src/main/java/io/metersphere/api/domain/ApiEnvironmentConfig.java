package io.metersphere.api.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class ApiEnvironmentConfig implements Serializable {
    @Schema(description =  "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_environment_config.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_environment_config.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "创建时间")
    private Long createTime;

    @Schema(description =  "修改时间")
    private Long updateTime;

    @Schema(description =  "用户fk")
    private String createUser;

    @Schema(description =  "环境fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_environment_config.environment_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_environment_config.environment_id.length_range}", groups = {Created.class, Updated.class})
    private String environmentId;

    private static final long serialVersionUID = 1L;
}