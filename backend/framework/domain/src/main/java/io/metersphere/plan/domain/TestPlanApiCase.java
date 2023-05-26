package io.metersphere.plan.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class TestPlanApiCase implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_api_case.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{test_plan_api_case.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "测试计划ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_api_case.test_plan_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_api_case.test_plan_id.length_range}", groups = {Created.class, Updated.class})
    private String testPlanId;

    @Schema(title = "接口用例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_api_case.api_case_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_api_case.api_case_id.length_range}", groups = {Created.class, Updated.class})
    private String apiCaseId;

    @Schema(title = "环境类型")
    private String environmentType;

    @Schema(title = "环境组ID")
    private String environmentGroupId;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_api_case.create_user.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 40, message = "{test_plan_api_case.create_user.length_range}", groups = {Created.class, Updated.class})
    private String createUser;

    @Schema(title = "自定义排序，间隔5000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_api_case.pos.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 19, message = "{test_plan_api_case.pos.length_range}", groups = {Created.class, Updated.class})
    private Long pos;

    @Schema(title = "所属环境")
    private String environment;

    private static final long serialVersionUID = 1L;
}