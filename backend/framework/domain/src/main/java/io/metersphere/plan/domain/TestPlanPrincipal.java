package io.metersphere.plan.domain;

import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@ApiModel(value = "测试计划责任人")
@Table("test_plan_principal")
@Data
public class TestPlanPrincipal implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @NotBlank(message = "{test_plan_principal.test_plan_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "测试计划ID", required = true, allowableValues = "range[1, 50]")
    private String testPlanId;

    @Id
    @NotBlank(message = "{test_plan_principal.user_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "用户ID", required = true, allowableValues = "range[1, 50]")
    private String userId;

}