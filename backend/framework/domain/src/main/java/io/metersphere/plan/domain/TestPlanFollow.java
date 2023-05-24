package io.metersphere.plan.domain;

import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@ApiModel(value = "测试计划关注人")
@Table("test_plan_follow")
@Data
public class TestPlanFollow implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotBlank(message = "{test_plan_follow.test_plan_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "测试计划ID", required = true, allowableValues = "range[1, 50]")
    private String testPlanId;

    @NotBlank(message = "{test_plan_follow.user_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "用户ID", required = true, allowableValues = "range[1, 50]")
    private String userId;


}