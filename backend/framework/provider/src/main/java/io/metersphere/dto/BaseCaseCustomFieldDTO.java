package io.metersphere.dto;

import io.metersphere.system.domain.CustomFieldOption;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class BaseCaseCustomFieldDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description =  "用例ID")
    private String caseId;

    @Schema(description =  "字段ID")
    private String fieldId;

    @Schema(description =  "字段值")
    private String defaultValue;

    @Schema(description =  "字段名称")
    private String fieldName;

    @Schema(description = "是否内置字段")
    private Boolean internal;

    @Schema(title = "选项值")
    private List<CustomFieldOption> options;

    @Schema(title = "字段类型")
    private String type;

}
