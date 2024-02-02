package io.metersphere.system.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterRequest {
    @NotBlank
    @Schema(description = "被邀请ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String inviteId;

    @NotBlank
    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 1, max = 255, message = "{user.name.length_range}")
    private String name;

    @NotBlank
    @Schema(description = "用户密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "用户手机号")
    private String phone;
}
