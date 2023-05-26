package io.metersphere.plan.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class TestPlanFollow implements Serializable {
    @Schema(title = "测试计划ID;联合主键", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_follow.test_plan_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_follow.test_plan_id.length_range}", groups = {Created.class, Updated.class})
    private String testPlanId;

    @Schema(title = "用户ID;联合主键", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_follow.user_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_follow.user_id.length_range}", groups = {Created.class, Updated.class})
    private String userId;

    private static final long serialVersionUID = 1L;
}