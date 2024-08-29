package io.metersphere.dto.lark;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "飞书")
@Data
public class LarkInfoDTO implements Serializable {
    @Schema(description = "应用ID")
    private String agentId;
    @Schema(description = "应用密钥")
    private String appSecret;
    @Schema(description = "是否开启")
    private Boolean enable = false;
    @Schema(description = "是否可用")
    private Boolean valid = false;
}
