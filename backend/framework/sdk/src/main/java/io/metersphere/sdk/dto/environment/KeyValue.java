package io.metersphere.sdk.dto.environment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
public class KeyValue {
    @Schema(description = "参数名")
    private String name;
    @Schema(description = "参数值")
    private String value;
    @Schema(description = "是否启用")
    private Boolean enable = true;

}
