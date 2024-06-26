package io.metersphere.system.dto.sdk;

import io.metersphere.system.domain.CustomFieldOption;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class TemplateCustomFieldDTO {

    @Schema(title = "字段ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fieldId;

    @Schema(title = "字段名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fieldName;

    @Schema(title = "字段唯一Key, 处理人,状态字段需要, 其余自定义字段ID即可标识唯一")
    private String fieldKey;

    @Schema(title = "是否必填")
    private Boolean required;

    @Schema(title = "api字段名")
    private String apiFieldId;

    @Schema(title = "默认值")
    private Object defaultValue;

    @Schema(title = "字段类型")
    private String type;

    @Schema(title = "选项值")
    private List<CustomFieldOption> options;

    @Schema(title = "是否支持搜索")
    private Boolean supportSearch;

    @Schema(title = "搜索调用方法")
    private String optionMethod;

    @Schema(description = "是否内置字段")
    private Boolean internal;

    /**
     * 平台字段相关属性    @start
     */
    @Schema(title = "平台选项值")
    private String platformOptionJson;

    @Schema(description = "平台字段占位提示")
    private String platformPlaceHolder;

    @Schema(description = "是否平台系统字段")
    private Boolean platformSystemField;

    @Schema(description = "内置字段的 key")
    private String internalFieldKey;
}
