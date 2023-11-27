package io.metersphere.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserLocalConfigAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "本地执行程序url", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_local_config.user_url.not_blank}")
    @Size(min = 1, max = 50, message = "{user_local_config.user_url.length_range}")
    private String userUrl;

    @Schema(description = "API/UI", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_local_config.type.not_blank}")
    @Size(min = 1, max = 50, message = "{user_local_config.type.length_range}")
    private String type = "API";

}