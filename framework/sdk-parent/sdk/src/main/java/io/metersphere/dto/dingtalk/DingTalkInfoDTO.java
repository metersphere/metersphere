package io.metersphere.dto.dingtalk;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "钉钉")
@Data
public class DingTalkInfoDTO implements Serializable {
    @Schema(description = "应用ID")
    private String agentId;
    @Schema(description = "应用key", requiredMode = Schema.RequiredMode.REQUIRED)
    private String appKey;
    @Schema(description = "应用密钥")
    private String appSecret;
    @Schema(description = "是否开启")
    private Boolean enable = false;
    @Schema(description = "是否可用")
    private Boolean valid = false;
}
