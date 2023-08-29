package io.metersphere.sdk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TemplateCustomFieldDTO {

    @Schema(title = "字段ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fieldId;

    @Schema(title = "字段名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fieldName;

    @Schema(title = "是否必填")
    private Boolean required;

    @Schema(title = "api字段名")
    private String apiFieldId;

    @Schema(title = "默认值")
    private String defaultValue;

}
