package io.metersphere.bug.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BugExportColumn {
    @Schema(description = "字段key", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String key;
    @Schema(description = "字段名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String text;
    @Schema(description = "字段类型: 系统字段-system, 自定义字段-custom, 其他字段-other")
    @NotBlank
    private String columnType;
}
