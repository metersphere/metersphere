package io.metersphere.dto.wecom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class WeComCreator implements Serializable {
    @Schema(description = "企业id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String corpId;
    @Schema(description = "应用ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String agentId;
    @Schema(description = "应用密钥", requiredMode = Schema.RequiredMode.REQUIRED)
    private String appSecret;
}
