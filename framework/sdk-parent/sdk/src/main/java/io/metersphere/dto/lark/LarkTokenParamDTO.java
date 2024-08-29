package io.metersphere.dto.lark;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "飞书")
@Data
public class LarkTokenParamDTO{
    @Schema(description = "code")
    public String code;
    @Schema(description = "grantType")
    public String grant_type;

}
