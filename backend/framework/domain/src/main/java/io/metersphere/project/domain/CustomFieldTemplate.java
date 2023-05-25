package io.metersphere.project.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class CustomFieldTemplate implements Serializable {
    @Schema(title = "自定义模版ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field_template.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{custom_field_template.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "自定义字段ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field_template.field_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{custom_field_template.field_id.length_range}", groups = {Created.class, Updated.class})
    private String fieldId;

    @Schema(title = "模版ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field_template.template_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{custom_field_template.template_id.length_range}", groups = {Created.class, Updated.class})
    private String templateId;

    @Schema(title = "使用场景", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field_template.scene.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 30, message = "{custom_field_template.scene.length_range}", groups = {Created.class, Updated.class})
    private String scene;

    @Schema(title = "是否必填")
    private Boolean required;

    @Schema(title = "排序字段")
    private Integer pos;

    @Schema(title = "自定义数据")
    private String customData;

    @Schema(title = "自定义表头")
    private String key;

    @Schema(title = "默认值")
    private byte[] defaultValue;

    private static final long serialVersionUID = 1L;
}