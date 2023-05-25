package io.metersphere.project.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class CustomField implements Serializable {
    @Schema(title = "自定义字段ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 100]")
    @NotBlank(message = "{custom_field.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 100, message = "{custom_field.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "自定义字段名称", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 64]")
    @NotBlank(message = "{custom_field.name.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 64, message = "{custom_field.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "使用场景", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 30]")
    @NotBlank(message = "{custom_field.scene.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 30, message = "{custom_field.scene.length_range}", groups = {Created.class, Updated.class})
    private String scene;

    @Schema(title = "自定义字段类型", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 30]")
    @NotBlank(message = "{custom_field.type.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 30, message = "{custom_field.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(title = "自定义字段备注")
    private String remark;

    @Schema(title = "是否是系统字段")
    private Boolean system;

    @Schema(title = "是否是全局字段")
    private Boolean global;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "创建人")
    private String createUser;

    @Schema(title = "项目ID")
    private String projectId;

    @Schema(title = "是否关联第三方", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 1]")
    @NotBlank(message = "{custom_field.third_part.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 1, message = "{custom_field.third_part.length_range}", groups = {Created.class, Updated.class})
    private Boolean thirdPart;

    @Schema(title = "自定义字段选项")
    private String options;

    private static final long serialVersionUID = 1L;
}