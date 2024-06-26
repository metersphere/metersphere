package io.metersphere.project.dto;

import io.metersphere.system.domain.CustomFieldOption;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author wx
 */
@Data
public class CustomFieldOptions implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private String id;

    @Schema(description = "自定义字段名称")
    private String name;

    @Schema(description = "自定义字段类型")
    private String type;

    @Schema(description = "是否内置字段")
    private Boolean internal;

    @Schema(description = "内置字段的 key")
    private String internalFieldKey;

    @Schema(description = "自定义字段选项值")
    private List<CustomFieldOption> options;
}
