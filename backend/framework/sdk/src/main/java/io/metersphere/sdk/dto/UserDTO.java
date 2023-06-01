package io.metersphere.sdk.dto;

import io.metersphere.system.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserDTO extends User {

    @Schema(title = "其他平台对接信息", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private byte[] platformInfo;

    @Schema(title = "UI本地调试地址", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String seleniumServer;
}
