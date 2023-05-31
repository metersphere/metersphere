package io.metersphere.api.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class ApiDefinitionModule implements Serializable {
    @Schema(title = "接口模块pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_module.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_definition_module.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "修改时间")
    private Long updateTime;

    @Schema(title = "修改人")
    private String updateUser;

    @Schema(title = "创建人")
    private String createUser;

    @Schema(title = "模块名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_module.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{api_definition_module.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "协议", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_module.protocol.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{api_definition_module.protocol.length_range}", groups = {Created.class, Updated.class})
    private String protocol;

    @Schema(title = "父级fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_module.parent_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_definition_module.parent_id.length_range}", groups = {Created.class, Updated.class})
    private String parentId;

    @Schema(title = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_module.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_definition_module.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "树节点级别", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_definition_module.level.not_blank}", groups = {Created.class})
    private Integer level;

    @Schema(title = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_definition_module.pos.not_blank}", groups = {Created.class})
    private Integer pos;

    private static final long serialVersionUID = 1L;
}