package io.metersphere.dto.dingtalk;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "钉钉基础信息")
@Data
public class DingTalkBaseParamDTO {
    @Schema(description = "应用key", requiredMode = Schema.RequiredMode.REQUIRED)
    private String appKey;
    @Schema(description = "应用密钥", requiredMode = Schema.RequiredMode.REQUIRED)
    private String appSecret;
}
