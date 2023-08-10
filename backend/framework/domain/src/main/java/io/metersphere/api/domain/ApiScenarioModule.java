package io.metersphere.api.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class ApiScenarioModule implements Serializable {
    @Schema(description =  "场景模块pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_module.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_scenario_module.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "模块名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_module.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{api_scenario_module.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description =  "模块级别", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_scenario_module.level.not_blank}", groups = {Created.class})
    private Integer level;

    @Schema(description =  "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_scenario_module.pos.not_blank}", groups = {Created.class})
    private Integer pos;

    @Schema(description =  "创建时间")
    private Long createTime;

    @Schema(description =  "更新时间")
    private Long updateTime;

    @Schema(description =  "更新人")
    private String updateUser;

    @Schema(description =  "创建人")
    private String createUser;

    @Schema(description =  "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_module.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_scenario_module.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description =  "父级fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_module.parent_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_scenario_module.parent_id.length_range}", groups = {Created.class, Updated.class})
    private String parentId;

    private static final long serialVersionUID = 1L;
}