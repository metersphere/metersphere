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

@ApiModel(value = "功能用例执行记录")
@TableName("test_plan_function_case_result")
@Data
public class TestPlanFunctionCaseResult implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{func_case_execution_info.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues="range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{func_case_execution_info.test_plan_function_case_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{func_case_execution_info.test_plan_function_case_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "测试计划功能用例关联表ID", required = true, allowableValues="range[1, 50]")
    private String testPlanFunctionCaseId;

    @Size(min = 1, max = 50, message = "{func_case_execution_info.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{func_case_execution_info.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目ID", required = true, allowableValues="range[1, 20]")
    private String projectId;

    @Size(min = 1, max = 50, message = "{func_case_execution_info.result.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{func_case_execution_info.result.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "执行结果", required = true, allowableValues="range[1, 50]")
    private String result;

    @Size(min = 1, max = 50, message = "{func_case_execution_info.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{func_case_execution_info.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues="range[1, 50]")
    private String createUser;

    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;

    @ApiModelProperty(name = "评论", allowableValues="range[1, 2000]")
    private String comment;

    @Size(min = 1, max = 50, message = "{func_case_execution_info.version.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{func_case_execution_info.version.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "所属版本", required = true, allowableValues="range[1, 50]")
    private String version;
}
