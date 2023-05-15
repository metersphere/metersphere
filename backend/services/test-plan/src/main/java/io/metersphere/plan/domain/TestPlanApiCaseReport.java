package io.metersphere.plan.domain;

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

@ApiModel(value = "测试计划接口用例执行结果")
@TableName("test_plan_api_case_report")
@Data
public class TestPlanApiCaseReport implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{test_plan_api_case_report.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "接口结果报告pk", required = true, allowableValues="range[1, 50]")
    private String id;

    @Size(min = 1, max = 200, message = "{test_plan_api_case_report.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_api_case_report.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "接口报告名称", required = true, allowableValues="range[1, 200]")
    private String name;

    @Size(min = 1, max = 50, message = "{test_plan_api_case_report.test_plan_api_case_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_api_case_report.test_plan_api_case_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "测试计划接口用例关联ID", required = true, allowableValues="range[1, 50]")
    private String testPlanApiCaseId;

    @Size(min = 1, max = 50, message = "{test_plan_api_case_report.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_api_case_report.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目ID", required = true, allowableValues="range[1, 50]")
    private String projectId;

    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;

    @Size(min = 1, max = 50, message = "{test_plan_api_case_report.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_api_case_report.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues="range[1, 50]")
    private String createUser;

    @ApiModelProperty(name = "修改时间", required = true, dataType = "Long")
    private Long updateTime;

    @Size(min = 1, max = 50, message = "{test_plan_api_case_report.update_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_api_case_report.update_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "修改人", required = true, allowableValues="range[1, 50]")
    private String updateUser;

    @Size(min = 1, max = 1, message = "{test_plan_api_case_report.deleted.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_api_case_report.deleted.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "删除状态", required = true, allowableValues="range[1, 1]")
    private Boolean deleted;

    @Size(min = 1, max = 50, message = "{test_plan_api_case_report.status.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_api_case_report.status.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "结果状态", required = true, allowableValues="range[1, 50]")
    private String status;

    @ApiModelProperty(name = "接口开始执行时间", dataType = "Long")
    private Long startTime;

    @ApiModelProperty(name = "接口执行结束时间", dataType = "Long")
    private Long endTime;

    @Size(min = 1, max = 20, message = "{test_plan_api_case_report.run_mode.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_api_case_report.run_mode.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "执行方式", required = true, allowableValues="range[1, 20]")
    private String runMode;

    @Size(min = 1, max = 50, message = "{test_plan_api_case_report.resource_pool.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_api_case_report.resource_pool.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "资源池", required = true, allowableValues="range[1, 50]")
    private String resourcePool;

    @Size(min = 1, max = 50, message = "{test_plan_api_case_report.trigger_mode.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_api_case_report.trigger_mode.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "触发模式", required = true, allowableValues="range[1, 50]")
    private String triggerMode;

    @Size(min = 1, max = 20, message = "{test_plan_api_case_report.execute_type.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_api_case_report.execute_type.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "执行类型", required = true, allowableValues="range[1, 20]")
    private String executeType;

    @Size(min = 1, max = 50, message = "{test_plan_api_case_report.version.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_api_case_report.version.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "所属版本", required = true, allowableValues="range[1, 50]")
    private String version;
}