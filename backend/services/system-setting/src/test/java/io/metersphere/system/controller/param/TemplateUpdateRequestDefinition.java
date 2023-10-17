package io.metersphere.system.controller.param;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TemplateUpdateRequestDefinition {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{template.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{template.id.length_range}", groups = {Updated.class})
    private String id;

    @Schema(title = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{template.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{template.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "备注")
    private String remark;

    @Schema(title = "是否是内置模板")
    private Boolean internal;

    @Schema(title = "创建时间")
    private Long updateTime;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "创建人")
    private String createUser;

    @Schema(title = "组织或项目级别字段（PROJECT, ORGANIZATION）")
    private String scopeType;

    @Schema(title = "组织或项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{template.scope_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{template.scope_id.length_range}", groups = {Created.class, Updated.class})
    private String scopeId;

    @Schema(title = "是否开启api字段名配置")
    private Boolean enableThirdPart;

    @Schema(title = "使用场景", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{template.scene.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 30, message = "{template.scene.length_range}", groups = {Created.class})
    private String scene;
}
