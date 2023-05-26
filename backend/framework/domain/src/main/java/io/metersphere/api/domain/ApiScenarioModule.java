package io.metersphere.api.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class ApiScenarioModule implements Serializable {
    @Schema(title = "场景模块pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_module.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_scenario_module.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "模块名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_module.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{api_scenario_module.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "模块级别", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_module.level.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 10, message = "{api_scenario_module.level.length_range}", groups = {Created.class, Updated.class})
    private Integer level;

    @Schema(title = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_module.pos.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 10, message = "{api_scenario_module.pos.length_range}", groups = {Created.class, Updated.class})
    private Integer pos;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "更新人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String updateUser;

    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String createUser;

    @Schema(title = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_module.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_scenario_module.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "父级fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_module.parent_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_scenario_module.parent_id.length_range}", groups = {Created.class, Updated.class})
    private String parentId;

    private static final long serialVersionUID = 1L;
}