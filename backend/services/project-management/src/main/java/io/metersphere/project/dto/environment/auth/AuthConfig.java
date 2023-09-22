package io.metersphere.project.dto.environment.auth;

import io.metersphere.project.dto.environment.ssl.KeyStoreConfig;
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

    @Schema(description = "认证方式 No Auth、Basic Auth、Digest Auth、ssl证书")
    private String verification;

    @Schema(description = "SSL配置")
    private KeyStoreConfig sslConfig;

    @Serial
    private static final long serialVersionUID = 1L;
}
