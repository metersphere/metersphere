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

@Schema(title = "场景模块")
@Table("api_scenario_module")
@Data
public class ApiScenarioModule implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{api_scenario_module.id.not_blank}", groups = {Updated.class})
    @Schema(title = "场景模块pk", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Size(min = 1, max = 64, message = "{api_scenario_module.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_module.name.not_blank}", groups = {Created.class})
    @Schema(title = "模块名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(title = "模块级别", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer level;

    @Schema(title = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer pos;

    @Schema(title = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long createTime;

    @Schema(title = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long updateTime;

    @Size(min = 1, max = 50, message = "{api_scenario_module.update_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_module.update_user.not_blank}", groups = {Created.class})
    @Schema(title = "更新人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String updateUser;

    @Size(min = 1, max = 50, message = "{api_scenario_module.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_module.create_user.not_blank}", groups = {Created.class})
    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String createUser;

    @Size(min = 1, max = 50, message = "{api_scenario_module.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_module.project_id.not_blank}", groups = {Created.class})
    @Schema(title = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;

    @Size(min = 1, max = 50, message = "{api_scenario_module.parent_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_module.parent_id.not_blank}", groups = {Created.class})
    @Schema(title = "父级fk", requiredMode = Schema.RequiredMode.REQUIRED)
    private String parentId;

}