
package io.metersphere.api.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "场景报告")
@Table("api_scenario_report")
@Data
public class ApiScenarioReport implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{api_scenario_report.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "场景报告pk", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 300, message = "{api_scenario_report.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_report.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "报告名称", required = true, allowableValues = "range[1, 300]")
    private String name;

    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;

    @Size(min = 1, max = 50, message = "{api_scenario_report.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_report.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues = "range[1, 50]")
    private String createUser;

    @ApiModelProperty(name = "删除时间", required = false, dataType = "Long")
    private Long deleteTime;

    @ApiModelProperty(name = "删除人", required = false, allowableValues = "range[1, 50]")
    private String deleteUser;

    @Size(min = 1, max = 1, message = "{api_scenario_report.deleted.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_report.deleted.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "删除标识", required = true, allowableValues = "range[1, 1]")
    private Boolean deleted;

    @Size(min = 1, max = 50, message = "{api_scenario_report.update_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_report.update_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "修改人", required = true, allowableValues = "range[1, 50]")
    private String updateUser;

    @ApiModelProperty(name = "更新时间", required = true, dataType = "Long")
    private Long updateTime;

    @ApiModelProperty(name = "通过率", required = false, dataType = "Long")
    private Long passRate;

    @Size(min = 1, max = 20, message = "{api_scenario_report.status.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_report.status.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "报告状态/SUCCESS/ERROR", required = true, allowableValues = "range[1, 20]")
    private String status;

    @Size(min = 1, max = 20, message = "{api_scenario_report.trigger_mode.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_report.trigger_mode.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "触发方式", required = true, allowableValues = "range[1, 20]")
    private String triggerMode;

    @Size(min = 1, max = 20, message = "{api_scenario_report.run_mode.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_report.run_mode.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "执行模式", required = true, allowableValues = "range[1, 20]")
    private String runMode;

    @Size(min = 1, max = 50, message = "{api_scenario_report.pool_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_report.pool_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "资源池", required = true, allowableValues = "range[1, 50]")
    private String poolId;


    @ApiModelProperty(name = "版本fk", required = false, allowableValues = "range[1, 50]")
    private String versionId;

    @Size(min = 1, max = 1, message = "{api_scenario_report.integrated.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_report.integrated.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "是否是集成报告", required = true, allowableValues = "range[1, 1]")
    private Boolean integrated;

    @Size(min = 1, max = 50, message = "{api_scenario_report.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_report.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目fk", required = true, allowableValues = "range[1, 50]")
    private String projectId;

    @Size(min = 1, max = 50, message = "{api_scenario_report.scenario_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_report.scenario_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "场景fk", required = true, allowableValues = "range[1, 50]")
    private String scenarioId;

}