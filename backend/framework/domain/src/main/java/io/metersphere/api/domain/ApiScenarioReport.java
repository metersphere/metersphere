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

@Schema(title = "场景报告")
@Table("api_scenario_report")
@Data
public class ApiScenarioReport implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{api_scenario_report.id.not_blank}", groups = {Updated.class})
    @Schema(title = "场景报告pk", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 300, message = "{api_scenario_report.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_report.name.not_blank}", groups = {Created.class})
    @Schema(title = "报告名称", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 300]")
    private String name;

    @Schema(title = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long createTime;

    @Size(min = 1, max = 50, message = "{api_scenario_report.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_report.create_user.not_blank}", groups = {Created.class})
    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String createUser;

    @Schema(title = "删除时间", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long deleteTime;

    @Schema(title = "删除人", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = "range[1, 50]")
    private String deleteUser;

    @Size(min = 1, max = 1, message = "{api_scenario_report.deleted.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_report.deleted.not_blank}", groups = {Created.class})
    @Schema(title = "删除标识", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 1]")
    private Boolean deleted;

    @Size(min = 1, max = 50, message = "{api_scenario_report.update_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_report.update_user.not_blank}", groups = {Created.class})
    @Schema(title = "修改人", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String updateUser;

    @Schema(title = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long updateTime;

    @Schema(title = "通过率", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long passRate;

    @Size(min = 1, max = 20, message = "{api_scenario_report.status.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_report.status.not_blank}", groups = {Created.class})
    @Schema(title = "报告状态/SUCCESS/ERROR", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 20]")
    private String status;

    @Size(min = 1, max = 20, message = "{api_scenario_report.trigger_mode.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_report.trigger_mode.not_blank}", groups = {Created.class})
    @Schema(title = "触发方式", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 20]")
    private String triggerMode;

    @Size(min = 1, max = 20, message = "{api_scenario_report.run_mode.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_report.run_mode.not_blank}", groups = {Created.class})
    @Schema(title = "执行模式", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 20]")
    private String runMode;

    @Size(min = 1, max = 50, message = "{api_scenario_report.pool_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_report.pool_id.not_blank}", groups = {Created.class})
    @Schema(title = "资源池", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String poolId;


    @Schema(title = "版本fk", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = "range[1, 50]")
    private String versionId;

    @Size(min = 1, max = 1, message = "{api_scenario_report.integrated.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_report.integrated.not_blank}", groups = {Created.class})
    @Schema(title = "是否是集成报告", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 1]")
    private Boolean integrated;

    @Size(min = 1, max = 50, message = "{api_scenario_report.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_report.project_id.not_blank}", groups = {Created.class})
    @Schema(title = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String projectId;

    @Size(min = 1, max = 50, message = "{api_scenario_report.scenario_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_report.scenario_id.not_blank}", groups = {Created.class})
    @Schema(title = "场景fk", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String scenarioId;

}