package io.metersphere.dto.dingtalk;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "钉钉")
@Data
public class DingTalkTokenParamDTO extends DingTalkBaseParamDTO {
    @Schema(description = "accessToken")
    public String accessToken;
    @Schema(description = "expireIn")
    public Long expireIn;
    @Schema(description = "clientId")
    public String clientId;
    @Schema(description = "clientSecret")
    public String clientSecret;
    @Schema(description = "code")
    public String code;
    @Schema(description = "grantType")
    public String grantType;

}
