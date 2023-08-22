package io.metersphere.sdk.dto.environment.authConfig;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AuthConfig implements Serializable {
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "密码")
    private String password;

    @Schema(description = "认证方式 No Auth、Basic Auth、Digest Auth")
    private String verification;

    @Serial
    private static final long serialVersionUID = 1L;
}
