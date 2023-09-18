package io.metersphere.project.dto.environment.assertions.document;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MsAssertionDocument {
    @Schema(description = "是否启用")
    private Boolean enable = true;
    @Schema(description = "断言类型 JSON/XML")
    private String type;
    @Schema(description = "数据")
    private Document data;
}
