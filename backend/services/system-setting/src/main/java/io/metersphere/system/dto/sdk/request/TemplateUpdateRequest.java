package io.metersphere.system.dto.sdk.request;

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
public class TemplateUpdateRequest {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{template.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{template.id.length_range}", groups = {Updated.class})
    private String id;

    @Schema(title = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{template.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{template.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "备注")
    @Size(max = 1000, groups = {Created.class, Updated.class})
    private String remark;

    @Schema(title = "组织或项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{template.scope_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{template.scope_id.length_range}", groups = {Created.class, Updated.class})
    private String scopeId;

    @Schema(title = "是否开启api字段名配置")
    private Boolean enableThirdPart;

    @Schema(title = "使用场景", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{template.scene.not_blank}", groups = {Created.class})
    @EnumValue(enumClass = TemplateScene.class, groups = {Created.class})
    @Size(min = 1, max = 30, message = "{template.scene.length_range}", groups = {Created.class})
    private String scene;

    @Valid
    @Schema(title = "自定义字段Id列表")
    private List<TemplateCustomFieldRequest> customFields;

    @Valid
    @Schema(title = "系统字段列表")
    private List<TemplateSystemCustomFieldRequest> systemFields;

    @Schema(description = "模板中新上传的文件ID列表")
    private List<String> uploadImgFileIds;
}
