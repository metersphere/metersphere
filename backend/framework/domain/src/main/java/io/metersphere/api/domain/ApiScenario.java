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

@Schema(title = "场景")
@Table("api_scenario")
@Data
public class ApiScenario implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{api_scenario.id.not_blank}", groups = {Updated.class})
    @Schema(title = "", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 200, message = "{api_scenario.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario.name.not_blank}", groups = {Created.class})
    @Schema(title = "场景名称", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 200]")
    private String name;

    @Size(min = 1, max = 50, message = "{api_scenario.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario.create_user.not_blank}", groups = {Created.class})
    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String createUser;

    @Schema(title = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long createTime;

    @Schema(title = "删除时间", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long deleteTime;

    @Schema(title = "删除人", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = "range[1, 50]")
    private String deleteUser;

    @Size(min = 1, max = 50, message = "{api_scenario.update_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario.update_user.not_blank}", groups = {Created.class})
    @Schema(title = "更新人", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String updateUser;


    @Schema(title = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long updateTime;

    @Size(min = 1, max = 10, message = "{api_scenario.level.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario.level.not_blank}", groups = {Created.class})
    @Schema(title = "场景级别/P0/P1等", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 10]")
    private String level;

    @Size(min = 1, max = 20, message = "{api_scenario.status.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario.status.not_blank}", groups = {Created.class})
    @Schema(title = "场景状态/未规划/已完成 等", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 20]")
    private String status;

    @Size(min = 1, max = 50, message = "{api_scenario.principal.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario.principal.not_blank}", groups = {Created.class})
    @Schema(title = "责任人/用户fk", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String principal;


    @Schema(title = "场景步骤总数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer stepTotal;

    @Schema(title = "通过率", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long passRate;

    @Schema(title = "最后一次执行的结果状态", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = "range[1, 50]")
    private String reportStatus;

    @Schema(title = "编号", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer num;

    @Schema(title = "自定义id", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = "range[1, 50]")
    private String customNum;

    @Size(min = 1, max = 1, message = "{api_scenario.deleted.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario.deleted.not_blank}", groups = {Created.class})
    @Schema(title = "删除状态", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 1]")
    private Boolean deleted;

    @Size(min = 1, max = 1, message = "{api_scenario.environment_group.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario.environment_group.not_blank}", groups = {Created.class})
    @Schema(title = "是否为环境组", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 1]")
    private Boolean environmentGroup;

    @Schema(title = "自定义排序", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long pos;

    @Schema(title = "版本fk", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = "range[1, 50]")
    private String versionId;

    @Schema(title = "引用资源fk", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = "range[1, 50]")
    private String refId;

    @Schema(title = "是否为最新版本 0:否，1:是", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = "range[1, 1]")
    private Boolean latest;

    @Size(min = 1, max = 50, message = "{api_scenario.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario.project_id.not_blank}", groups = {Created.class})
    @Schema(title = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String projectId;

    @Schema(title = "场景模块fk", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = "range[1, 50]")
    private String apiScenarioModuleId;

    @Schema(title = "最后一次执行的报告fk", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = "range[1, 50]")
    private String reportId;

    @Schema(title = "描述信息", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = "range[1, 500]")
    private String description;

    @Schema(title = "模块全路径/用于导入模块创建", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = "range[1, 1000]")
    private String modulePath;

    @Schema(title = "标签", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = "range[1, 500]")
    private String tags;

}