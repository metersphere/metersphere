package io.metersphere.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserLocalConfigUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_local_config.id.not_blank}")
    private String id;

    @Schema(description = "本地执行程序url")
    @Size( max = 50, message = "{user_local_config.user_url.length_range}")
    private String userUrl;


}