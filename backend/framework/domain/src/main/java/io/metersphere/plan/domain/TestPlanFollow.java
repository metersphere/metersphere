package io.metersphere.plan.domain;

import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "测试计划关注人")
@Table("test_plan_follow")
@Data
public class TestPlanFollow implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotBlank(message = "{test_plan_follow.test_plan_id.not_blank}", groups = {Updated.class})
    @Schema(title = "测试计划ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String testPlanId;

    @NotBlank(message = "{test_plan_follow.user_id.not_blank}", groups = {Updated.class})
    @Schema(title = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String userId;


}