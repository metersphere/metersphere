
package io.metersphere.api.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "API/CASE执行结果")
@TableName("api_report")
@Data
public class ApiReport implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{api_report.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "接口结果报告pk", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 200, message = "{api_report.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_report.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "接口报告名称", required = true, allowableValues = "range[1, 200]")
    private String name;

    @Size(min = 1, max = 50, message = "{api_report.resource_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_report.resource_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "资源fk/api_definition_id/api_test_case_id", required = true, allowableValues = "range[1, 50]")
    private String resourceId;

    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;

    @ApiModelProperty(name = "修改时间", required = true, dataType = "Long")
    private Long updateTime;

    @Size(min = 1, max = 50, message = "{api_report.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_report.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人fk", required = true, allowableValues = "range[1, 50]")
    private String createUser;

    @Size(min = 1, max = 50, message = "{api_report.update_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_report.update_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "修改人", required = true, allowableValues = "range[1, 50]")
    private String updateUser;

    @Size(min = 1, max = 1, message = "{api_report.deleted.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_report.deleted.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "删除状态", required = true, allowableValues = "range[1, 1]")
    private Boolean deleted;

    @Size(min = 1, max = 50, message = "{api_report.status.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_report.status.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "结果状态", required = true, allowableValues = "range[1, 50]")
    private String status;


    @ApiModelProperty(name = "接口开始执行时间", required = false, dataType = "Long")
    private Long startTime;


    @ApiModelProperty(name = "接口执行结束时间", required = false, dataType = "Long")
    private Long endTime;

    @Size(min = 1, max = 20, message = "{api_report.run_mode.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_report.run_mode.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "执行模块/API/CASE/API_PLAN", required = true, allowableValues = "range[1, 20]")
    private String runMode;

    @Size(min = 1, max = 50, message = "{api_report.resource_pool.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_report.resource_pool.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "资源池", required = true, allowableValues = "range[1, 50]")
    private String resourcePool;

    @Size(min = 1, max = 50, message = "{api_report.trigger_mode.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_report.trigger_mode.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "触发模式/手动/批量/定时任务/JENKINS", required = true, allowableValues = "range[1, 50]")
    private String triggerMode;


    @ApiModelProperty(name = "版本fk", required = false, allowableValues = "range[1, 50]")
    private String versionId;

    @Size(min = 1, max = 50, message = "{api_report.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_report.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目fk", required = true, allowableValues = "range[1, 50]")
    private String projectId;

    @Size(min = 1, max = 50, message = "{api_report.integrated_report_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_report.integrated_report_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "集成报告id/api_scenario_report_id", required = true, allowableValues = "range[1, 50]")
    private String integratedReportId;

    @Size(min = 1, max = 1, message = "{api_report.integrated.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_report.integrated.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "是否为集成报告，默认否", required = true, allowableValues = "range[1, 1]")
    private Boolean integrated;

}