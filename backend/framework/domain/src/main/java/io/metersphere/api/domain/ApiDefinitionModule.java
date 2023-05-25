package io.metersphere.api.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "接口模块")
@Table("api_definition_module")
@Data
public class ApiDefinitionModule implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{api_definition_module.id.not_blank}", groups = {Updated.class})
    @Schema(title = "接口模块pk", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(title = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long createTime;

    @Schema(title = "修改时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long updateTime;

    @Size(min = 1, max = 50, message = "{api_definition_module.update_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition_module.update_user.not_blank}", groups = {Created.class})
    @Schema(title = "修改人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String updateUser;

    @Size(min = 1, max = 50, message = "{api_definition_module.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition_module.create_user.not_blank}", groups = {Created.class})
    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String createUser;

    @Size(min = 1, max = 64, message = "{api_definition_module.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition_module.name.not_blank}", groups = {Created.class})
    @Schema(title = "模块名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(min = 1, max = 64, message = "{api_definition_module.protocol.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition_module.protocol.not_blank}", groups = {Created.class})
    @Schema(title = "协议", requiredMode = Schema.RequiredMode.REQUIRED)
    private String protocol;

    @Size(min = 1, max = 50, message = "{api_definition_module.parent_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition_module.parent_id.not_blank}", groups = {Created.class})
    @Schema(title = "父级fk", requiredMode = Schema.RequiredMode.REQUIRED)
    private String parentId;

    @Size(min = 1, max = 50, message = "{api_definition_module.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition_module.project_id.not_blank}", groups = {Created.class})
    @Schema(title = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;

    @Schema(title = "树节点级别", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer level;

    @Schema(title = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer pos;

}