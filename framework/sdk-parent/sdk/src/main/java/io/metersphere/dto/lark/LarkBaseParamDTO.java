package io.metersphere.dto.lark;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "飞书基础信息")
@Data
public class LarkBaseParamDTO {
    @Schema(description = "应用ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String app_id;
    @Schema(description = "应用密钥", requiredMode = Schema.RequiredMode.REQUIRED)
    private String app_secret;
}
