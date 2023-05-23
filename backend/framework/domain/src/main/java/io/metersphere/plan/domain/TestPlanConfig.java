package io.metersphere.plan.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@ApiModel(value = "测试计划配置")
@Table("test_plan_config")
@Data
public class TestPlanConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @NotBlank(message = "{test_plan_config.test_plan_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "测试计划ID", required = true, allowableValues = "range[1, 50]")
    private String testPlanId;


    @ApiModelProperty(name = "运行模式", required = true)
    private String runModeConfig;

    @Size(min = 1, max = 1, message = "{test_plan_config.automatic_status_update.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_config.automatic_status_update.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "是否自定更新功能用例状态", required = true, allowableValues = "range[1, 1]")
    private Boolean automaticStatusUpdate;

    @Size(min = 1, max = 1, message = "{test_plan_config.repeat_case.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_config.repeat_case.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "是否允许重复添加用例", required = true, allowableValues = "range[1, 1]")
    private Boolean repeatCase;

    @Size(min = 1, max = 3, message = "{test_plan_config.pass_threshold.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_config.pass_threshold.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "测试计划通过阈值", required = true, dataType = "Integer")
    private Integer passThreshold;

}