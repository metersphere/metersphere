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
    @Schema(description =  "接口模块pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_module.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_definition_module.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "创建时间")
    private Long createTime;

    @Schema(description =  "修改时间")
    private Long updateTime;

    @Schema(description =  "修改人")
    private String updateUser;

    @Schema(description =  "创建人")
    private String createUser;

    @Schema(description =  "模块名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_module.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{api_definition_module.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description =  "协议", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_module.protocol.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{api_definition_module.protocol.length_range}", groups = {Created.class, Updated.class})
    private String protocol;

    @Schema(description =  "父级fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_module.parent_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_definition_module.parent_id.length_range}", groups = {Created.class, Updated.class})
    private String parentId;

    @Schema(description =  "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_module.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_definition_module.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description =  "树节点级别", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_definition_module.level.not_blank}", groups = {Created.class})
    private Integer level;

    @Schema(description =  "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_definition_module.pos.not_blank}", groups = {Created.class})
    private Integer pos;

    private static final long serialVersionUID = 1L;
}