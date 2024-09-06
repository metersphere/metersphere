package io.metersphere.system.dto.sdk.request;

import io.metersphere.sdk.constants.CustomFieldType;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.valid.EnumValue;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CustomFieldUpdateRequest {
    @Schema(title = "自定义字段ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{custom_field.id.length_range}", groups = {Updated.class})
    private String id;

    @Schema(title = "自定义字段名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{custom_field.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "使用场景", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field.scene.not_blank}", groups = {Created.class})
    @EnumValue(enumClass = TemplateScene.class, groups = {Created.class})
    @Size(min = 1, max = 30, message = "{custom_field.scene.length_range}", groups = {Created.class})
    private String scene;

    @Schema(title = "自定义字段类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field.type.not_blank}", groups = {Created.class})
    @EnumValue(enumClass = CustomFieldType.class, groups = {Created.class, Updated.class})
    @Size(min = 1, max = 30, message = "{custom_field.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(title = "自定义字段备注")
    @Size(max = 1000, message = "{custom_field.remark.length_range}", groups = {Created.class, Updated.class})
    private String remark;

    @Schema(title = "组织或项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field.scope_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{custom_field.scope_id.length_range}", groups = {Created.class})
    private String scopeId;

    @Schema(description = "是否需要手动输入选项key")
    private Boolean enableOptionKey;

    @Valid
    @Schema(title = "自定义字段选项")
    private List<CustomFieldOptionRequest> options;
}
