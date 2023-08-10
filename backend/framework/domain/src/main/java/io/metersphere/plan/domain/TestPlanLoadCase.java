package io.metersphere.plan.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class TestPlanLoadCase implements Serializable {
    @Schema(description =  "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_load_case.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{test_plan_load_case.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "测试计划ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_load_case.test_plan_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_load_case.test_plan_id.length_range}", groups = {Created.class, Updated.class})
    private String testPlanId;

    @Schema(description =  "性能用例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_load_case.load_case_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_load_case.load_case_id.length_range}", groups = {Created.class, Updated.class})
    private String loadCaseId;

    @Schema(description =  "创建时间")
    private Long createTime;

    @Schema(description =  "创建人")
    private String createUser;

    @Schema(description =  "所用测试资源池ID")
    private String testResourcePoolId;

    @Schema(description =  "自定义排序，间隔5000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_load_case.pos.not_blank}", groups = {Created.class})
    private Long pos;

    @Schema(description =  "压力配置")
    private String loadConfiguration;

    @Schema(description =  "高级配置")
    private String advancedConfiguration;

    private static final long serialVersionUID = 1L;
}